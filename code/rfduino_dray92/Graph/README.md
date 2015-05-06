#### Developed by Debosmit Ray, UW CSE477 2015

The code is released under the GNU General Public License.
_________

This is the [Processing](http://processing.org/) application I made to visualize the data sent from the Arduino.

This should be used with the [MPU-6050] code I have provided.

Note that you properly need to change the serial port for it to work.

The serial port can be changed at the following line in the code:

```
serial = new Serial(this, Serial.list()[0], 9600);
```

For more information fell free to post a question at the guide: <http://arduino.cc/forum/index.php/topic,58048.0.html> or send me an email at <dray92@uw.edu>.