# EMonitor_App
Android App to monitor and estimate the energy consumption details of an apartment

As a part of my Masters' thesis work , I have developed a mobile app called EMonitor that would enable the residents of the apartments to monitor their energy consumption levels.
\
The features which are included with this app are:\
1  The user is able to view the current day's power consumption of his/her Apartment with 10-minutes sampling interval.\
2  The user can compare his consumption with the average, minimum and maximum levels of the building and also that of other apartments(Privacy settings are included here)\
3  User can also view the historical statistics of their power consumption in the following granularity levels.\
    Day-Wise: User can select a particular date and view his consumption.\
    Month-Wise : User can view patterns month wise\
    Year-wise  : User can see historical patterns in based on year.\
4  Even in the statistics section, user can compare his data to others at all levels of granularity. \
5  We also included a feature that would give average power consumption for the time range selected and the estimated bill amount.\
   So by this the user can find out if the power consumption is high, medium or low and can take necessary measures if required.\
6  We also implemented a privacy feature in this app whereby , the faculty members(users) with registered official email-id can have access to install this app.\
   In case if app is installed with any other email-id, then a push notification is sent to the official user of this app who can either accept or reject the request.
   This would be useful if other family members of the same apartments wants to use the app.\
 

Technologies Used:
Front-End :As it is an android app, the front end is developed using XML and Android programming.\
Back-End  : We have used Firebase as back-end database for our app.\
App interacts with back-end through REST API  calls.\
For getting the dynamic real time data which at a private cloud server, we have used R code to extract that dynamic and then uploaded to firebase database using a python script which has the facility to connect with firebase database. \

This python script is scheduled to run every 10 minutes that will extract data from portal and upload it into firebase database.

