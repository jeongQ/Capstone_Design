var udp = require('dgram');
var mysql = require('mysql');
var dt = require('date-utils')
var twilio = require('twilio');
// --------------------creating a udp server --------------------

//variables
var co2 = '';
var hr = '';
var brain = '';
var count = 0;
var co2flag;
var hrflag;
var brainflag;
var helpflag = 0;
var count;
var tmp1='';
var tmp2='';

// creating a udp server
var server = udp.createSocket('udp4');

// --------------------connecting to database -------------------
var db = mysql.createConnection({
host : 'localhost',
port : 3306,
user : "root",
password : "tbd",
database : "TBD"
});

var insertuser = 'INSERT INTO user (name,birthDate,sex,id,password) VALUES (?,?,?,?,?)';
var select = 'SELECT password from user';
var insertdata = 'INSERT INTO SensorData (date,CO2,HeartRate,BrainWave) VALUES (?,?,?,?)';
// -----------------------sms --------------------
var account = 'AC8bbba43d07f9bf7bfc2b51a0b1531c9a';
var token = '6c270f01a7fcd54dbeba78f9f97f612a';
var client = new twilio(account,token);

// emits when any error occurs
server.on('error',function(error){
                console.log('Error: ' + error);
                server.close();
                });

// emits on new datagram msg
server.on('message',function(msg,info){

                var t = new Date();
                var d = t.toFormat('YYYY-MM-DD HH24:MI:SS');


                var param = msg.toString();
                var arry = param.split(':');

                //signup
                if(arry[0]=='signup'){
                        var newarr = arry.slice(1,6);
                        db.query(insertuser,newarr,function(err,rows,fields){
                                console.log(newarr);
                        });
                }

                //android
                if(arry[0]=='co2_start'){
                        server.send(co2,info.port,info.address,function(error){
                                if(error) console.log('fail to send co2 to android');
                        })
                }
                if(arry[0]=='heart_start'){
                        server.send(hr,info.port,info.address,function(error){
                                if(error) console.log('fail to send hr to android');
                        })
                }

if(arry[0]=='brain_start'){
                        server.send(brain,info.port,info.address,function(error){
                                if(error) console.log('fail to send brain to android');
                        });
                };


                //co2 button
                if(arry[0]=='CO2'){

                        console.log('Data received from client : ' + msg.toString());
                        co2 = arry[1];
                }
                if(arry[0]=='HT'){

                        console.log('Data received from client : ' + msg.toString());
                        hr = arry[1];
                        if(tmp1=='') tmp1 = parseInt(hr);
                        else if(tmp2==''){
                                tmp2 = parseInt(hr);

                                console.log(Math.abs(tmp1-tmp2));
                                if(Math.abs(tmp1-tmp2)>20){
                                        client.messages.create({
                                                body : "Emergency situation. Help!! ",
                                                to: "+821097777647",
                                                from: "+17123509886"
                                        },function(err,message){
                                        if(err) console.log(err);
                                        else console.log(message.sid);
                                        });
                                }
                                tmp1 = tmp2;
                                tmp2 = '';
                        }
                }

if(arry[0]=='Br'){
                        
                        console.log('Data received from client : ' + msg.toString());
                        var tmp = arry[1];
                        if(tmp[0] == '0') brain = '00';
                        else brain = tmp.slice(0,2);
                }
                
                if(count == 20){
                        var param = [d,co2,hr,brain];   
                        db.query(insertdata,param,function(err,rows,fields){
                                if(err) console.log('fail to save data (co2)');
                        });
                        count = 0;
                }
                count++;

});

//emits when socket is ready and listening for datagram msgs
server.on('listening',function(){
                var address = server.address();
                var port = address.port;
                var family = address.family;
                var ipaddr = address.address;
                console.log('Server is listening at port' + port);
                console.log('Server ip :' + ipaddr);
                console.log('Server is IP4/IP6 : ' + family);

                });

//emits after the socket is closed using socket.close();
server.on('close',function(){
                console.log('Socket is closed !');
                });

server.bind(2222);
