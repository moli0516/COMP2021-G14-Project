"use strict";
const roomTypes=["meetingRoom","seminarRoom","computerLab","lectureTheatre"];
const operations={
 Session:[["login",[["userId","text"]]],["logout",[]],["quit",[]]],
 Rooms:[["listAllRooms",[]],["listRoom",[["roomId","text"]]],["search",[["date","date"],["startTime","time"],["endTime","time"],["minCapacity","number"],["features","text"]]]],
 Bookings:[["listBookings",[]],["book",[["roomId","text"],["date","date"],["startTime","time"],["endTime","time"],["purpose","text"]]],["modifyBooking",[["bookingId","text"],["roomId","text"],["date","date"],["startTime","time"],["endTime","time"],["purpose","text"]]],["deleteBooking",[["bookingId","text"]]]],
 Schedule:[["schedule",[["roomId","text"],["weekStartDate","date"]]]],
 Administration:[["addRoom",[["roomType",roomTypes],["capacity","number"],["features","text"]]],["modifyRoom",[["roomId","text"],["field",["roomType","capacity","features"]],["newValue","text"]]],["deleteRoom",[["roomId","text"]]],["addUser",[["name","text"]]],["removeUser",[["userId","text"]]],["changeRole",[["userId","text"],["role",["REGULAR","ADMIN"]]]]]
};
const labels={login:"Log in",logout:"Log out",quit:"Quit application",listAllRooms:"List all rooms",listRoom:"Room details",search:"Search available rooms",listBookings:"My bookings",book:"Create booking",modifyBooking:"Modify booking",deleteBooking:"Delete booking",schedule:"Weekly schedule",addRoom:"Add room",modifyRoom:"Modify room",deleteRoom:"Delete room",addUser:"Add user",removeUser:"Remove user",changeRole:"Change role"};

let busy=false,closed=false,signedIn=false,currentSection="Session";
let roomOptions=[],bookingOptions=[];
let roomMessage="Log in, then refresh options",bookingMessage="Log in, then refresh options";
const knownUsers=new Map([["SA001","SA001 · superadmin"]]);
const manualValue="__manual__";

function parseOptions(output,kind){
 const text=output.trim();
 if(/^Error:/i.test(text))throw new Error(text);
 const empty=kind==="room"
     ? /^(?:No rooms(?: were)?(?: found| available)?|No available rooms)\.?$/i
     : /^No bookings(?: found)?\.?$/i;
 if(empty.test(text))return [];
 if(!text)throw new Error("Empty response");
 const pattern=kind==="room"?/^(R\d+)\b/:/^(B\d+)\b/;
 const result=new Map();
 for(const raw of text.split(/\r?\n/)){
  const line=raw.trim();if(!line)continue;
  const match=line.match(pattern);
  if(!match)throw new Error("Unrecognized "+kind+" output: "+line);
  if(kind==="booking"&&/\bCANCELLED\b/i.test(line))continue;
  result.set(match[1],{value:match[1],label:line});
 }
 return [...result.values()];
}
async function sendCommand(line,automatic=false){
 const response=await fetch("/api/command",{
  method:"POST",headers:{"Content-Type":"text/plain; charset=utf-8",
   "X-CROBS-Token":document.querySelector('meta[name="crobs-token"]').content},body:line});
 const output=await response.text();
 const li=document.createElement("li");li.textContent=(automatic?"[options refresh] ":"")+line;
 document.getElementById("activity").prepend(li);
 if(!response.ok)throw new Error(output||"HTTP "+response.status);
 return output;
}
async function loadOptions(){
 if(!signedIn||closed)return;
 try{roomOptions=parseOptions(await sendCommand("listAllRooms",true),"room");roomMessage="";}
 catch(error){roomOptions=[];roomMessage="Rooms unavailable: "+error.message;}
 try{bookingOptions=parseOptions(await sendCommand("listBookings",true),"booking");bookingMessage="";}
 catch(error){bookingOptions=[];bookingMessage="Bookings unavailable: "+error.message;}
 updatePickers();
}
function makeOption(value,label){const option=document.createElement("option");option.value=value;option.textContent=label;return option;}
function pickerData(name,command){
 if(name==="roomId")return {items:roomOptions,message:roomMessage||"Select a room"};
 if(name==="bookingId")return {items:bookingOptions,message:bookingMessage||"Select your booking"};
 return {items:[...knownUsers].filter(([id])=>command==="login"||id!=="SA001")
      .map(([value,label])=>({value,label})),message:"Select a known user"};
}
function fillPicker(select,preserve=true){
 const data=pickerData(select.dataset.idField,select.dataset.command);
 const old=preserve?select.value:"";
 select.replaceChildren(makeOption("",data.message));
 for(const item of data.items)select.append(makeOption(item.value,item.label));
 select.append(makeOption(manualValue,"Enter ID manually…"));
 if(old===manualValue||data.items.some(item=>item.value===old))select.value=old;
 else select.value="";
}
function updatePickers(){document.querySelectorAll("select[data-id-field]").forEach(select=>fillPicker(select));}
function buildIdField(label,name,command){
 const select=document.createElement("select");select.name=name;select.required=true;
 select.dataset.idField=name;select.dataset.command=command;fillPicker(select,false);
 const manual=document.createElement("input");manual.type="text";manual.hidden=true;
 manual.setAttribute("aria-label","Manual "+name);manual.placeholder="Enter "+name;
 select._manual=manual;
 select.addEventListener("change",()=>{
  const enabled=select.value===manualValue;manual.hidden=!enabled;manual.required=enabled;
  if(enabled)manual.focus();else manual.value="";
 });
 label.append(select,manual);
 if(name==="userId"){
  const note=document.createElement("small");note.className="muted";
  note.textContent="Known users on this page only; use manual ID for other accounts.";label.append(note);
 }
}
function readField(form,name){
 const field=form.elements.namedItem(name);
 return (field.value===manualValue&&field._manual?field._manual.value:field.value).trim();
}
function updateSession(command,values,output){
 if(command==="login"){
  signedIn=true;knownUsers.set(values[0],values[0]);
  const match=output.match(/Logged in as\s+(\S+?)\((.*?)\)/);
  if(match)knownUsers.set(values[0],match[1]+" · "+match[2]);
 }else if(command==="logout"||command==="quit"){
  signedIn=false;roomOptions=[];bookingOptions=[];
  roomMessage="Log in, then refresh options";bookingMessage=roomMessage;
  if(command==="quit")closed=true;
 }else if(command==="addUser"){
  const match=output.match(/User added:\s*(U\d+)\((.*?)\)/);
  if(match)knownUsers.set(match[1],match[1]+" · "+match[2]);
 }else if(command==="removeUser")knownUsers.delete(values[0]);
 else if(command==="changeRole"&&knownUsers.has(values[0]))knownUsers.set(values[0],values[0]+" · "+values[1]);
}
function render(section){
 currentSection=section;
 document.getElementById("section-title").textContent=section;
 document.querySelectorAll("nav button").forEach(button=>button.classList.toggle("active",button.textContent===section));
 const root=document.getElementById("forms");root.replaceChildren();
 const refresh=document.createElement("button");refresh.type="button";refresh.dataset.refresh="true";refresh.textContent="Refresh ID options";
 refresh.addEventListener("click",async()=>{
  if(busy||closed)return;
  if(!signedIn){show("Log in first to load room and booking options.",true);return;}
  busy=true;setDisabled();
  try{await loadOptions();show(roomMessage||bookingMessage||"ID options refreshed",!!(roomMessage||bookingMessage));}
  finally{busy=false;setDisabled();}
 });
 const note=document.createElement("p");note.className="muted";
 note.textContent="Options are parsed from existing command output. Refresh queries also appear in logs. All tabs share one login; use one tab.";
 root.append(refresh,note);
 for(const [command,fields]of operations[section]){
  const form=document.createElement("form"),heading=document.createElement("h3"),row=document.createElement("div");
  heading.textContent=labels[command];row.className="fields";form.append(heading,row);
  for(const[name,type]of fields){
   const label=document.createElement("label");label.textContent=name;
   if(["roomId","bookingId","userId"].includes(name))buildIdField(label,name,command);
   else{
    const input=document.createElement(Array.isArray(type)?"select":"input");input.name=name;input.required=true;
    if(Array.isArray(type)){for(const value of type)input.append(makeOption(value,value));}
    else{input.type=type;if(type==="number"){input.min=name==="minCapacity"?"0":"1";input.step="1";}if(name==="features")input.placeholder="projector,whiteboard or -";}
    label.append(input);
   }
   row.append(label);
  }
  const button=document.createElement("button");button.type="submit";button.textContent=labels[command];row.append(button);
  form.addEventListener("submit",async event=>{
   event.preventDefault();if(busy||closed)return;
   const values=fields.map(([name])=>readField(form,name));
   for(let i=0;i<values.length;i++){
    const name=fields[i][0];
    if(!values[i]||/[\r\n]/.test(values[i])||(!["name","purpose"].includes(name)&&/\s/.test(values[i]))){show("Use a nonblank value; spaces are allowed only in name and purpose.",true);return;}
   }
   if(["deleteRoom","deleteBooking","removeUser","quit"].includes(command)&&!confirm("Confirm: "+labels[command]+"?"))return;
   busy=true;setDisabled();
   try{
    const output=await sendCommand([command,...values].join(" "));
    const failed=/^\s*Error:/i.test(output);show(output,failed);
    if(!failed){
     updateSession(command,values,output);
     if(signedIn)await loadOptions();
     updatePickers();
     if(closed)document.getElementById("status").textContent="Application stopped. Restart Java and reload this page.";
    }
   }catch(error){show("Request failed. The action may have completed; do not blindly retry. Check the server. "+error.message,true);}
   finally{busy=false;setDisabled();}
  });
  root.append(form);
 }
 setDisabled();
}
function setDisabled(){
 document.querySelectorAll('button[type="submit"],button[data-refresh],nav button').forEach(button=>button.disabled=busy||closed);
}
function show(text,error){
 document.getElementById("output").textContent=text;
 const status=document.getElementById("status");status.textContent=error?"Operation failed":"Response received";status.className=error?"error":"success";
}
for(const section of Object.keys(operations)){
 const button=document.createElement("button");button.type="button";button.textContent=section;
 button.addEventListener("click",()=>{if(!busy&&!closed)render(section);});document.getElementById("navigation").append(button);
}
render("Session");