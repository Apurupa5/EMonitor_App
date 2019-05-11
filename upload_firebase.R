library(fireData)
library(RSmap)
library(xts)
#RSmap("http://energy.iiitd.edu.in:9102/backend")
RSmap("http://iiitdarchiver.zenatix.com:9105")
yr<-"_2017"
start <- as.numeric(strptime(Sys.Date(), "%Y-%m-%d"))*1000
end <- as.numeric(strptime("4-5-2018", "%m-%d-%Y"))*1000
currtime<-as.numeric(Sys.time())*1000
end<-currtime
default_path = "./"
oat <- list(
  #"15c3e2dc-3868-5d11-9ada-2346ed712e18" ## working for 302 power
  # "aab8a341-2b43-590e-94bd-d1248946f6c1" ## working for 302 energy
  "9f161747-5ada-54e0-8367-9a66f31d4ebb"  ## 802 power
)


data <- RSmap.data_uuid(oat, start, end)
#cat("Data is\n")


i=1
dframe=as.data.frame(cbind(data[[i]]$time,data[[i]]$value))
names(dframe) = c("timestamp","power")
#names(dframe) = c("timestamp","energy")
dframe$timestamp = as.POSIXct(dframe$timestamp/1000,origin="1970-01-01")
setwd("C:/Users/NB VENKATESHWARULU/Desktop/Thesis_Work/Data/Energy/dynamic/")

#filename<-paste(files[[j]],yr)
filename1<-paste("Apartment802_dynamic",".csv")
filename1<-gsub(" ", "", filename1)
#cat("\n")
#cat(filename1)
write.csv(dframe,file=filename1,row.names=F,quote=F)
#cat(" Done\n")

head(dframe,10)

#(x = dframe, projectURL = "https://energyapp-7492d.firebaseio.com/", directory = "testing_R/-L9HNCvKo-3dQ7EOgx2U")
