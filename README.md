# MultithreadHauntedHouse
Simulates a haunted house ride - where multiple passengers (haunted house attendees) go on multiple car rides.

<b>Note: This program is multithreaded, and the threads are synchronized through the use of semaphores.</b>

Program simulates a haunted house tour. Each haunted house attendee is simulated with a new thread, and additionally, each car that gives
tours is simulated by a thread as well.

The rules of the ride are as follows: Each passenger must go on the ride X amount of times before they leave the haunted house. After
each ride, a passenger will wander around the haunted house for a short period (sleep) and then, if they need to go on the ride again,
will get back on the line.

Car tours last for a random time, and multiple cars can give tours simultaneously (there is no guarantee that cars will return in the
same order that they departed the station).

The user can specify the following: The number of passengers in the park, the number of cars that are being used for tours, and the
number of passengers that can fit in each car. These values can be specified as command line arguments in the given order, represented
by integers. If all of these values aren't specified, the program will use default values of 11 passengers, 3 cars, and 4 passengers per car.
Note: It is possible, depending on the given specifications, that a car will have to give a tour with less than the specified passengers
per car number (This happens once, if, for example, there are only 2 passengers left who want to take tours - the car will accept these
passengers, despite the specified passengers per car value being 4).

After completing all their rides (in the default case, 3 tours), passengers will not get back on line, but will instead "wander" around
the park. Due to specifications provided by the professor, passengers will leave in descending order (that is, passenger-n leaves first,
passenger-1 leaves last). Once a passenger leaves, the thread simulating said passenger is terminated. The last passenger to leave will
in turn terminate the car threads.
