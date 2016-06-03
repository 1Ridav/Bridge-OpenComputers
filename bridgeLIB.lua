local HOST = "bridge.computercraft.ru:1111"
local commandIdent = "+#"
local deviceCommandIdent = "+$"
local internet
local socket, connectedToBridge, connectedToDevice, KEY
local msgBuffer = {}
local BR = {}

function BR.split(source, delimiters)
  local elements = {}
  local pattern = '([^'..delimiters..']+)'
  string.gsub(source, pattern, function(value) elements[#elements + 1] =     value;  end);
  return elements
end

local function tableToJSON(arr)
  local json = "{"
  local delim = ""
  for k, v in pairs(arr) do
    json = json .. delim .."\"" .. tostring(k) .. "\":\"" .. tostring(v) .. "\""
    delim = ","
  end
  json = json.."}"
  return json
end

local function enqueue(array, n)
  n = n or 1
  for i = n, #array do
    table.insert(msgBuffer, array[i])
  end
end

local function sendInternal(str)
  if not str or str == "" then return false end
  local data = str.."\0"
  local buff
  local i = 0
  while i < 4 do
    buff = socket.write(data)
    if not buff then
      connectedToBridge = false
      return false
    elseif buff > 0 then
      return true
    end
    -- RETRY IF BUFF IS 0
    i = i + 1
  end

  return true  
end


function BR.isBridgeConnected() return connectedToBridge end
function BR.isPairConnected() return connectedToDevice end

function BR.sendNotification(notif)
  sendInternal(deviceCommandIdent .. "31 " .. tableToJSON(notif))
end

function BR.sendToast(text, duration)
  duration = duration or 0
  sendInternal(deviceCommandIdent .. "30 " .. tostring(duration) .. " " .. text)
end

function BR.send(str)
  if not str or str == "" then return false end
  return sendInternal("-"..str)
end

local function execCommand(cmd)
  local cmdNum = string.sub(cmd, 3, 4)

  if     cmdNum == "00" then 
    --ping
    sendInternal(string.sub(cmd, 1, 2) .. "01") --REPLY TO SENDER
  elseif cmdNum == "01" then
    --pong
  elseif cmdNum == "02" then
    connectedToBridge = false
    --bridge stopped
  elseif cmdNum == "10" then
    connectedToDevice = false
    --no pair
  elseif cmdNum == "20" then
    connectedToDevice = true
    --pair found
  elseif cmdNum == "21" then
    connectedToDevice = false
  end
end

local function isCommand(str)
  if string.sub(str, 1, 1) == "+" then
    return true
  end
  return false
end

function BR.receive()
  local buff
  if connectedToBridge then
    buff = socket.read()
    if buff == nil then --CONNECTION LOST
      connectedToBridge = false
      connectedToDevice = false
      return nil, false
    elseif #msgBuffer > 0 then --IF BUFFER HAS DATA
      if buff ~= "" then --IF MESSAGE IS NOT EMPTY
        enqueue(BR.split(buff, "\0"))
      end

      buff = table.remove(msgBuffer, 1) --GET FIRST MESSAGE FROM BUFFER
    elseif buff == "" then
      return buff, connectedToDevice
    end

    local m = BR.split(buff, "\0")
    if #m > 1 then
      enqueue(m, 2)
      buff = m[1]
    end

    while true do
      if isCommand(buff) then
        execCommand(buff)
        --return "", connectedToDevice
        if(#msgBuffer > 0) then
          buff = table.remove(msgBuffer, 1)
        else
          return "", connectedToDevice
        end
      else
        buff = BR.split(buff, "\0")[1] --CUT \0
        if buff == nil then return "", connectedToDevice end   
        return string.sub(buff, 2, #buff), connectedToDevice
      end
    end
  end    
  return nil, false
end

function BR.init(key, newHost)
  if not key or key == "" then return false end
  KEY = key
  HOST = newHost or HOST
  internet = require("component").internet
  socket = internet.connect(HOST)
  if not socket then return false end
  local i = 0
  while not socket.finishConnect() do 
    if i >= 4 then
      return false
    end 
    i = i + 1
    os.sleep(0.1) 
  end

  sendInternal("KOC-"..key)
  connectedToDevice = false
  connectedToBridge = true
  return true
end

return BR
