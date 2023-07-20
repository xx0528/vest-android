
javascript:
window.jsBridge.postMessage = function(eventName, params) {
    var currency = '';
    var amount = -1.0;
    if (eventName === 'firstrecharge' || eventName === 'recharge' || eventName === 'withdrawOrderSuccess') {
        var arr = JSON.parse(params);
        amount = arr.amount;
        currency = arr.currency;
    }
    var obj = {
        'method': 'event',
        'eventType': 'af',
        'amount': amount,
        'currency': currency,
        'eventName': eventName,
        'param': params,
    }
    window.androidJs.onCall(JSON.stringify(obj)) ;
}