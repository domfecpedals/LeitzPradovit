# Leitz Pradovit Auto Focus Module <h1>
=============

This project is aim to add auto focus function to the vintage Leitz Pradovit projector
The project is based on android phone and arduino microcontroller, phone and arduino are connected via bluetooth. 


The arduino takes care of the command reading and driving the fucos motor.
A serial bluetooth module is used to communicate with the android, total of 3 command can be received, Left, right and stop. One command can be sent from arduino to android, which is the slide advanced signal to inform the android that the module is ready for a new run of focusing.
The wiring of the arduino side can be found here:

The android phone uses camera to read the projected image. A sobel operation is used to determine if the image is on focus. The more the image out of focus, the smaller the total gradient magnitude value from sobel operator, vise versa. When focusing, calculate the gradient magnitude value of current image, set a initial turn to the motor, after a predefined interval of time, calculate the gradient value again and compare it with the previous one, if the difference value is positive, the motor keeps running in this direction, otherwise, the phone send out the command asking the motor to trun in the oppersite direction. Every time the motor change its direction, the interval decrease. Finally the motor will stop when the peak of gradient value is found, now we can say that the projected image is in focus. This process can be described as HILL CLIMBING algorithm, for details: http://en.wikipedia.org/wiki/Hill_climbing.

The block diagram of the android side program can be found here:
