// Trends in the log
// -----------------
// Find the trends in your log by getting some statistics, rinse, repeat, wait, and look again.
// 
// es@udp.no - Espen Schulstad in 2011

if (this.args.size() !=1 )  {
  println "This script expects 1 path"
  return
}

if(!new File(args[0]).exists()){
	println "Didn't find the file: " + args[0]	
	return
}

String logpath = args[0]

println "\nFind the exceptions with the highest frequency in the log\nRinse and repeat over time to start seeing patterns."
println "\nStarting .. \n\nWill try to find statistics from the file: " + logpath

// Count all mentions of exceptions
String catStatement = 'cat ' + logpath

def allExceptionsProc = catStatement.execute() | 'grep -c Exception'.execute()
println "\nCount of lines with \"Exception\" in file: " + allExceptionsProc.text

// Count unique lines of exceptions
def uniqueExceptionCountProc = catStatement.execute() | 'grep Exception '.execute() |  'sort'.execute() |  'uniq'.execute() | 'wc -l'.execute()
println "Count of unique lines with \"Exception\" in file: " + uniqueExceptionCountProc.text

// Let's do the hard work unique exceptions
def uniqueExceptionProc = catStatement.execute() | 'grep -n Exception'.execute() |  'sort'.execute() |  'uniq'.execute()  

println "Processing all exceptions"
// For each unique exception, find out how many times they occur and at what lines they occur. Insert into map.
def map = [:]
uniqueExceptionProc.text.eachLine{line,index ->
	def linenumber = line.trim().split(":")[0]
	def linevalue = line.minus(linenumber + ":")
	if(line!= null && linenumber != null){
		if(map.containsKey(linevalue)){
			map.put(linevalue, map.get(linevalue) << "," +linenumber)
		} else {
			map.put(linevalue,linenumber)			
		}
	}
}

map = map.sort { a, b -> b.value.length() <=> a.value.length()}

def date = new Date().format("dd-MM-yyyy")
println "Writing the values to file: results-${date}.csv"

def result = "Rank;Count;Error;Can be found at line"
map.eachWithIndex{ key, value, index ->
	def count = value.toString().split(',').size()
    index = index + 1
	result += "\n$index;$count;$key;\"$value\"" 
}

def file = new File("results-${date}.csv").write(result)

println "Done"