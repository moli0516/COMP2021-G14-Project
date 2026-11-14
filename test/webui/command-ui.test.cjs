"use strict";
const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");
class Node {
 constructor(tag){this.tagName=tag;this.children=[];this.dataset={};this.classList={toggle(){}};this.value="";this.textContent="";}
 append(...nodes){this.children.push(...nodes);}
 prepend(...nodes){this.children.unshift(...nodes);}
 replaceChildren(...nodes){this.children=[...nodes];}
 setAttribute(name,value){this[name]=value;}
 addEventListener(){}
 remove(){}
}
const ids=new Map();
const document={createElement:tag=>new Node(tag),getElementById:id=>{if(!ids.has(id))ids.set(id,new Node("div"));return ids.get(id);},querySelectorAll:()=>[],querySelector:()=>({content:"token"})};
const file=process.argv[2]||"webui/app.js";
const context=vm.createContext({document,console,Map,fetch:()=>{throw new Error("Unexpected network request");},confirm:()=>true});
vm.runInContext(fs.readFileSync(file,"utf8"),context);
const run=code=>vm.runInContext(code,context);
let checks=0;
function check(code){assert.ok(run(code),code);checks++;}
check('Object.values(operations).flat().length===17');
check('Object.keys(labels).length===19');
check('parseOptions("R000001 | meetingRoom | capacity: 10 | features []","room")[0].value==="R000001"');
check('parseOptions("B000001 room R000001 2030-01-01 10:00-11:00 hi ACTIVE","booking").length===1');
check('parseOptions("B000001 room R000001 2030-01-01 10:00-11:00 hi CANCELLED","booking").length===0');
check('parseOptions("No rooms were found.","room").length===0');
check('parseOptions("No bookings found.","booking").length===0');
check('(()=>{try{parseOptions("Error: forbidden","room");return false;}catch(e){return true;}})()');
const examples={
 listAllRooms:"R000001 | meetingRoom | capacity: 10 | features [projector]",
 listRoom:"R000001 | meetingRoom | capacity: 10 | features []",
 search:"R000001 | meetingRoom | capacity: 10 | features []",
 addRoom:"Room added: R000001 | meetingRoom | capacity: 10 | features []",
 modifyRoom:"Room modified: R000001 | meetingRoom | capacity: 20 | features []",
 listBookings:"B000001 room R000001 2030-01-01 10:00-11:00 demo ACTIVE",
 book:"Booking created: B000001 room R000001 2030-01-01 10:00-11:00 demo ACTIVE",
 modifyBooking:"Booking modified: B000001 room R000001 2030-01-01 12:00-13:00 demo ACTIVE",
 schedule:"B000001 | 2030-01-01 10:00-11:00 | purpose | with pipes | booked by hidden",
 login:"Logged in as SA001(superadmin, SUPERADMIN)",
 addUser:"User added: U000001(Ada Example, REGULAR)",
 changeRole:"Role changed: U000001(Ada Example, ADMIN)"
};
for(const[command,output]of Object.entries(examples)){
 context.sample={command,output};check('formattedOutput(sample.command,sample.output).className==="table-scroll"');
}
for(const command of ['deleteRoom','deleteBooking','removeUser','logout','quit','undo','redo']){
 context.sample={command,output:"Operation completed"};check('formattedOutput(sample.command,sample.output).className==="message-body"');
}
check('formattedOutput("listAllRooms","New format: preserve me").children[0].textContent==="New format: preserve me"');
check('formattedOutput("listBookings","No bookings found.").className==="message-body"');
check('formattedOutput("schedule","B000001 | 2030-01-01 10:00-11:00 | hi | booked by hidden").children[0].children[1].children[0].children[5].textContent==="hidden"');
check('(()=>{const attack="<img src=x onerror=alert(1)>";return formattedOutput("unknown",attack).children[0].textContent===attack;})()');
check('(()=>{showResult({command:"undo",ok:false,output:"Error: Nothing to undo"});return document.getElementById("output").className==="result result-error";})()');
check('(()=>{syncSession({userId:"SA001",userRole:"SUPERADMIN",canUndo:true,canRedo:false});return !document.getElementById("undo").disabled&&document.getElementById("redo").disabled;})()');
check('(()=>{syncSession({userId:"",userRole:"",canUndo:false,canRedo:false});return document.getElementById("undo").disabled&&document.getElementById("redo").disabled;})()');
console.log(JSON.stringify({checks,passed:true,scope:"JavaScript syntax and mock-DOM rendering; not a browser smoke test"}));
