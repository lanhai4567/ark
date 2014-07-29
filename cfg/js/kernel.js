function ConvertMilliseconds(dateStr) {
	if (dateStr) {
		var result = dateStr.replace(/(^\s*)|(\s*$)/g, "");
		var vals = result.split(".");
		if (vals.length > 0) {
			result = vals[0].replace(/-/g, "/");
			var _dateTime = new Date(result);
			result = _dateTime.getTime() + Math.ceil(vals[1]);
		} else {
			result = result.replace(/-/g, "/");
			var _dateTime = new Date(str);
			result = _dateTime.getTime();
		}
		if (isNaN(result))
			return 0; 
		return result
	}
	return 0;
}

function ForDight(Dight,How){  
	Dight = Math.round(Dight*Math.pow(10,How))/Math.pow(10,How);  
    return Dight;  
}

function interval(dateEndStr1, dateStartStr2) {
	if (dateEndStr1 && dateStartStr2) {
		var num1 = ConvertMilliseconds(dateEndStr1);
		if(num1 != 0){
			var num2 = ConvertMilliseconds(dateStartStr2);
			if(num2 != 0){
				if(num1<num2){
					return 0;
				}
				return ForDight((num1 - num2)/1000,3);
			}
		}
	}
	return 0;
}

function countRate(fileSize,dateEndStr1,dateStartStr2){
	if(fileSize && fileSize != 0){
		var delay = interval(dateEndStr1,dateStartStr2);
		if(delay != 0){
			return ForDight(fileSize/delay,2);
		}
	}
	return 0.0;
}
function isNull(str) {
	return this[str] == null;
}
function isChangeAccountAgain(A,accountA,B,accountB){
	if(A.resultCode == 'ENCOUNTER_VERIFICATION' && accountA.length > 1){
		if(B == null ||(B.resultCode == 'ENCOUNTER_VERIFICATION' && accountB.length > 1)||B.resultCode == 'SUCCESS'){
			return true;
		}
	}else if(A.resultCode == 'SUCCESS' && B != null && B.resultCode == 'ENCOUNTER_VERIFICATION' && accountB.length > 1){
		return true;
	}
	return false;
}
function getAccountName(A,AA,account){
	if(A.resultCode == 'SUCCESS'){
		return account[0].name;
	}else if(A.resultCode == 'ENCOUNTER_VERIFICATION' && AA && AA.resultCode == 'SUCCESS'){
		return account[1].name;
	}
	return '';
}

function divide(a,b){
	var result;
	if(a==null||b==null||b==0){
		result=0;
	}else{
		result = ForDight(a/b,2);
	}
	return result;
}
