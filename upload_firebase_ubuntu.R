library(fireData)
library(RSmap)
library(xts)

#RSmap("http://energy.iiitd.edu.in:9102/backend")
RSmap("http://iiitdarchiver.zenatix.com:9105")

#args = commandArgs(trailingOnly=TRUE)
#cat(args)

start <- as.numeric(strptime(Sys.Date(), "%Y-%m-%d"))*1000

currtime<-as.numeric(Sys.time())*1000
end<-currtime
default_path = "./"


#args <- strsplit(args,",") #split the string argument with ','
#args <- as.data.frame(args)

#id <- toString(args[[2]])
#name <- toString(args[[1]])

id<-"15c3e2dc-3868-5d11-9ada-2346ed712e18"
name<-"Apartment302"
oat<-id
try({
  
  data <- RSmap.data_uuid(oat, start, end)
})
i=1
dframe=as.data.frame(cbind(data[[i]]$time,data[[i]]$value))
if(length(dframe)==0)
{
  dframe
}
if(length(dframe)!=0)
{
  names(dframe) = c("timestamp","power")
  
  
  dframe$timestamp = as.POSIXct(dframe$timestamp/1000,origin="1970-01-01")
  
  #setwd("C:/Users/NB VENKATESHWARULU/Desktop/Thesis_Work/Data/Energy/dynamic/")
  dir="C:/Users/NB VENKATESHWARULU/Desktop/Thesis_Work/Data/Energy/dynamic/"
  
  yr<-"_dynamic"
  name<-paste(name,yr)
  
  
  filename1<-paste(name,".csv")
  filename1<-gsub(" ", "", filename1)
  filename1<-paste(dir,filename1)
  
  write.csv(dframe,file=filename1,row.names=F,quote=F)
  
  dframe
}