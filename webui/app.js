"use strict";
const roomTypes=["meetingRoom","seminarRoom","computerLab","lectureTheatre"];
const operations={
 Session:[["login",[["userId","text"]]],["logout",[]],["quit",[]]],
 Rooms:[["listAllRooms",[]],["listRoom",[["roomId","text"]]],["search",[["date","date"],["startTime","time"],["endTime","time"],["minCapacity","number"],["features","text"]]]],
 Bookings:[["listBookings",[]],["book",[["roomId","text"],["date","date"],["startTime","time"],["endTime","time"],["purpose","text"]]],["modifyBooking",[["bookingId","text"],["roomId","text"],["date","date"],["startTime","time"],["endTime","time"],["purpose","text"]]],["deleteBooking",[["bookingId","text"]]]],
 Schedule:[["schedule",[["roomId","text"],["weekStartDate","date"]]]],
 Administration:[["addRoom",[["roomType",roomTypes],["capacity","number"],["features","text"]]],["modifyRoom",[["roomId","text"],["field",["roomType","capacity","features"]],["newValue","text"]]],["deleteRoom",[["roomId","text"]]],["addUser",[["name","text"]]],["removeUser",[["userId","text"]]],["changeRole",[["userId","text"],["role",["REGULAR","ADMIN"]]]]]
};
const labels={login:"Log in",logout:"Log out",quit:"Quit application",listAllRooms:"List all rooms",listRoom:"Room details",search:"Search available rooms",listBookings:"My bookings",book:"Create booking",modifyBooking:"Modify booking",deleteBooking:"Delete booking",schedule:"Weekly schedule",addRoom:"Add room",modifyRoom:"Modify room",deleteRoom:"Delete room",addUser:"Add user",removeUser:"Remove user",changeRole:"Change role",undo:"Undo",redo:"Redo"};
const $=id=>document.getElementById(id);
const manualValue="__manual__", knownUsers=new Map([["SA001","SA001 · superadmin"]]);
let busy=false,closed=false,signedIn=false,currentUserId="",canUndo=false,canRedo=false;
let roomOptions=[],bookingOptions=[],roomMessage="Log in, then refresh options",bookingMessage=roomMessage;
function element(tag,text,className){const node=document.createElement(tag);if(text!==undefined)node.textContent=text;if(className)node.className=className;return node;}
function makeOption(value,label){const node=element("option",label);node.value=value;return node;}
function parseOptions(output,kind){
 const text=output.trim();if(/^Error:/i.test(text))throw new Error(text);
 if((kind==="room"?/^(?:No rooms(?: were)?(?: found| available)?|No available rooms)\.?$/i:/^No bookings(?: found)?\.?$/i).test(text))return [];
 if(!text)throw new Error("Empty response");
 const result=new Map(),pattern=kind==="room"?/^(R\d+)\b/:/^(B\d+)\b/;
 for(const raw of text.split(/\r?\n/)){const line=raw.trim();if(!line)continue;const match=line.match(pattern);
  if(!match)throw new Error("Unrecognized "+kind+" output");
  if(kind==="booking"&&/\bCANCELLED\s*$/.test(line))continue;
  result.set(match[1],{value:match[1],label:line});
 }return [...result.values()];
}
function syncSession(result){
 signedIn=!!result.userId;currentUserId=result.userId;canUndo=!!result.canUndo;canRedo=!!result.canRedo;
 if(signedIn)knownUsers.set(currentUserId,currentUserId+" · "+result.userRole);
 $("session-state").textContent=signedIn?currentUserId+" · "+result.userRole:"Not logged in";
 if(!signedIn){roomOptions=[];bookingOptions=[];roomMessage="Log in, then refresh options";bookingMessage=roomMessage;}
 setDisabled();
}
async function sendCommand(line,automatic=false){
 const response=await fetch("/api/command",{method:"POST",headers:{"Content-Type":"text/plain; charset=utf-8","Accept":"application/json","X-CROBS-Token":document.querySelector('meta[name="crobs-token"]').content},body:line});
 const body=await response.text();
 $("activity").prepend(element("li",(automatic?"[options refresh] ":"")+line));
 while($("activity").children.length>100)$("activity").lastElementChild.remove();
 if(!response.ok)throw new Error(body||"HTTP "+response.status);
 let result;try{result=JSON.parse(body);}catch(error){throw new Error("Invalid server result; restart the patched Java server and reload this page");}
 if(typeof result.output!=="string"||typeof result.ok!=="boolean"||typeof result.userId!=="string")throw new Error("Invalid command response");
 syncSession(result);return result;
}
async function loadOptions(){
 if(!signedIn||closed)return;
 try{roomOptions=parseOptions((await sendCommand("listAllRooms",true)).output,"room");roomMessage="";}catch(error){roomOptions=[];roomMessage="Rooms unavailable: "+error.message;}
 try{bookingOptions=parseOptions((await sendCommand("listBookings",true)).output,"booking");bookingMessage="";}catch(error){bookingOptions=[];bookingMessage="Bookings unavailable: "+error.message;}
 updatePickers();
 $("option-status").textContent=roomMessage||bookingMessage||"Room and booking options up to date.";
}
function pickerData(name,command){
 if(name==="roomId")return {items:roomOptions,message:roomMessage||"Select a room"};
 if(name==="bookingId")return {items:bookingOptions,message:bookingMessage||"Select your booking"};
 return {items:[...knownUsers].filter(([id])=>command==="login"||id!=="SA001").map(([value,label])=>({value,label})),message:"Select a page-known user"};
}
function fillPicker(select,preserve=true){
 const data=pickerData(select.dataset.idField,select.dataset.command),old=preserve?select.value:"";
 select.replaceChildren(makeOption("",data.message));for(const item of data.items)select.append(makeOption(item.value,item.label));
 select.append(makeOption(manualValue,"Enter ID manually…"));
 select.value=old===manualValue||data.items.some(item=>item.value===old)?old:"";
}
function updatePickers(){document.querySelectorAll("select[data-id-field]").forEach(select=>fillPicker(select));}
function buildIdField(label,name,command){
 const select=document.createElement("select"),manual=document.createElement("input");select.name=name;select.required=true;
 select.dataset.idField=name;select.dataset.command=command;fillPicker(select,false);
 manual.type="text";manual.hidden=true;manual.setAttribute("aria-label","Manual "+name);manual.placeholder="Enter "+name;select._manual=manual;
 select.addEventListener("change",()=>{const enabled=select.value===manualValue;manual.hidden=!enabled;manual.required=enabled;if(enabled)manual.focus();else manual.value="";});
 label.append(select,manual);
 if(name==="userId")label.append(element("small","Known users on this page only; use manual ID for others.","muted"));
}
function readField(form,name){const field=form.elements.namedItem(name);return(field.value===manualValue&&field._manual?field._manual.value:field.value).trim();}
function table(headers,rows){
 const wrap=element("div",undefined,"table-scroll"),node=document.createElement("table"),head=document.createElement("thead"),tr=document.createElement("tr");
 for(const title of headers){const th=element("th",title);th.scope="col";tr.append(th);}head.append(tr);node.append(head);
 const body=document.createElement("tbody");for(const row of rows){const line=document.createElement("tr");for(const value of row)line.append(element("td",value));body.append(line);}node.append(body);wrap.append(node);return wrap;
}
function formattedOutput(command,output){
 const lines=output.trim().split(/\r?\n/).filter(Boolean);
 const room=/^(?:[^\r\n]*?:\s*)?(R\d+) \| (\w+) \| capacity: (\d+) \| features (\[.*\])$/;
 const booking=/^(?:[^\r\n]*?:\s*)?(B\d+) room (R\d+) (\d{4}-\d{2}-\d{2}) (\S+)-(\S+) (.*) (ACTIVE|CANCELLED)$/;
 const schedule=/^(B\d+) \| (\d{4}-\d{2}-\d{2}) (\S+)-(\S+) \| (.*) \| booked by (\S+)$/;
 if(["login","addUser","changeRole"].includes(command)&&lines.length===1){
  const user=lines[0].match(/(?:^|:\s*|as\s+)((?:U\d+|SA001))\((.*),\s*(REGULAR|ADMIN|SUPERADMIN)\)$/);
  if(user)return table(["User","Name","Role"],[user.slice(1)]);
 }
 let headers,pattern;
 if(["listRoom","listAllRooms","search","addRoom","modifyRoom"].includes(command)){headers=["Room","Type","Capacity","Features"];pattern=room;}
 else if(["listBookings","book","modifyBooking"].includes(command)){headers=["Booking","Room","Date","Start","End","Purpose","Status"];pattern=booking;}
 else if(command==="schedule"){headers=["Booking","Date","Start","End","Purpose","Owner"];pattern=schedule;}
 if(pattern&&lines.length){const rows=lines.map(line=>line.match(pattern));if(rows.every(Boolean))return table(headers,rows.map(match=>match.slice(1)));}
 // Messages, user details, empty lists, errors and future formats remain lossless.
 const message=element("div",undefined,"message-body");for(const line of lines)message.append(element("p",line));return message;
}
function showResult(result){
 const root=$("output");root.replaceChildren();root.className="result "+(result.ok?"result-success":"result-error");
 const top=element("div",undefined,"result-heading");top.append(element("h3",labels[result.command]||result.command||"Response"),element("span",result.ok?"Completed":"Failed","result-badge"));root.append(top);
 root.append(formattedOutput(result.command,result.output));
 const details=document.createElement("details");details.append(element("summary","Raw command output"),element("pre",result.output));root.append(details);
 $("status").textContent=result.ok?"Response received":"Operation failed";$("status").className=result.ok?"success":"error";
}
function show(text,error){showResult({command:"Response",ok:!error,output:text});}
function rememberUser(command,values,output){
 if(command==="addUser"){const match=output.match(/User added:\s*(U\d+)\((.*),\s*(REGULAR|ADMIN)\)/);if(match)knownUsers.set(match[1],match[1]+" · "+match[2]);}
 if(command==="removeUser")knownUsers.delete(values[0]);
 if(command==="changeRole"&&knownUsers.has(values[0]))knownUsers.set(values[0],values[0]+" · "+values[1]);
 if(["undo","redo","logout","login"].includes(command)){knownUsers.clear();knownUsers.set("SA001","SA001 · superadmin");if(currentUserId)knownUsers.set(currentUserId,currentUserId);}
}
async function execute(command,values=[]){
 if(busy||closed)return;
 if(["deleteRoom","deleteBooking","removeUser","quit"].includes(command)&&!confirm("Confirm: "+labels[command]+"?"))return;
 busy=true;setDisabled();
 try{
  const result=await sendCommand([command,...values].join(" "));showResult(result);
  if(result.ok){rememberUser(command,values,result.output);if(command==="quit"){closed=true;$("status").textContent="Application stopped. Restart Java and reload this page.";}
   if(signedIn&&!closed)await loadOptions();updatePickers();}
 }catch(error){show("Request failed. The action may have completed; do not blindly retry. Check the server. "+error.message,true);}
 finally{busy=false;setDisabled();}
}
function render(section){
 $("section-title").textContent=section;document.querySelectorAll("nav button").forEach(button=>button.classList.toggle("active",button.textContent===section));
 const root=$("forms");root.replaceChildren();
 for(const[command,fields]of operations[section]){
  const form=document.createElement("form"),row=element("div",undefined,"fields");form.append(element("h3",labels[command]),row);
  if(command==="book"||command==="modifyBooking"){
   form.append(element("p",
    "Start must be in the future in the server's local time zone. Today's later times are allowed. Invalid times are shown as operation errors after submission.",
    "muted"));
  }
  for(const[name,type]of fields){const label=element("label",name);
   if(["roomId","bookingId","userId"].includes(name))buildIdField(label,name,command);
   else{const input=document.createElement(Array.isArray(type)?"select":"input");input.name=name;input.required=true;
    if(Array.isArray(type)){for(const value of type)input.append(makeOption(value,value));}else{input.type=type;if(type==="number"){input.min=name==="minCapacity"?"0":"1";input.step="1";}if(name==="features")input.placeholder="projector,whiteboard or -";}label.append(input);}
   row.append(label);
  }
  const button=element("button",labels[command]);button.type="submit";row.append(button);
  form.addEventListener("submit",event=>{event.preventDefault();const values=fields.map(([name])=>readField(form,name));
   for(let i=0;i<values.length;i++){const name=fields[i][0];if(!values[i]||/[\r\n]/.test(values[i])||(!["name","purpose"].includes(name)&&/\s/.test(values[i]))){show("Use a nonblank value; spaces are allowed only in name and purpose.",true);return;}}
   execute(command,values);
  });root.append(form);
 }setDisabled();
}
function setDisabled(){
 document.querySelectorAll('button[type="submit"],#refresh-options,nav button').forEach(button=>button.disabled=busy||closed);
 $("undo").disabled=busy||closed||!signedIn||!canUndo;$("redo").disabled=busy||closed||!signedIn||!canRedo;
}
for(const section of Object.keys(operations)){const button=element("button",section);button.type="button";button.addEventListener("click",()=>{if(!busy&&!closed)render(section);});$("navigation").append(button);}
$("undo").addEventListener("click",()=>execute("undo"));$("redo").addEventListener("click",()=>execute("redo"));
$("refresh-options").addEventListener("click",async()=>{if(busy||closed)return;if(!signedIn){show("Log in first.",true);return;}busy=true;setDisabled();try{await loadOptions();}finally{busy=false;setDisabled();}});
render("Session");
