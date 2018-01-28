var functions = require('firebase-functions');
var admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.getSensorInfo = functions.https.onRequest((req, res) => {
    
    var time = req.query.time;
    
    admin.database().ref(time).once('value').then(snap => {
        
        var object = snap.val();
        var keys = Object.keys(object);
        var result = object[keys[keys.length-1]];
        console.log(keys[keys.length-1]);
        return res.send(JSON.stringify(result));
    });
    
});
