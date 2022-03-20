# snapshot-utility
This is an image grabber application for Fajr project. The purpose of this app is to schedule image recording from a source. In Pre Alpha release, it supports computer displays (basically graphic card buffer). We can improve it to support any other image generator such camera, HDMI inputs, SDI...

This app has a test mode for test/debug. We can change this setup in Menu > Screenshots > TEST Mode. 

if "TEST Mode" is checked, this app will repeat a scheduled task each minute. 

A schdeduled Task has a start time , end time and interval. 

How to schedule screenshots:
===========================

1 - Step 1:
Screenshots > New screenshot

![Screen Shot 2022-03-20 at 12 01 23 PM](https://user-images.githubusercontent.com/6443429/159171231-85ef2eba-dd6f-4b0a-81f4-cfc04b001194.png)


2 - Step 2:

Enter settings for a screenshot session:

![Screen Shot 2022-03-20 at 12 03 45 PM](https://user-images.githubusercontent.com/6443429/159171306-9c87cb4f-c86f-4d2e-ac26-47322f1d54c7.png)

Output Folder : the folder where screenshots will land.
Start time : screenshot start date time
End Time:screenshot end date time
Take screenshots every (mm:ss) :period between screenshots
Select Rectanlgle button : Drag your mouse to select the region in the screen to grab and Hit <ENTER>
Select CheckBox TEST Mode:(in REAL mode, it diaplays "Run this task periodically")   : If selected, this session repeat each minute in TEST Mode.
  In REAL Mode, this session will repeat each x hours x=1..24
  
  Then Save
  
  
3-Monitor your session:
  This app will show running/end/scheduled status
  To remove a schedule, right click on the id and select "REMOVE". This action will end and remove the current session
  ![Screen Shot 2022-03-20 at 12 13 25 PM](https://user-images.githubusercontent.com/6443429/159171837-dd5ce832-2ca4-4675-96a0-4482ef2a38d3.png)

  
  



