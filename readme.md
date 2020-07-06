
# Wave Engine
**Wave Engine** is easy to use, ECS-based, 2D, multithreaded, Java game engine. Currently, it is extremely WIP and under-documented.
  
## Entity Component System
### What is ECS?
According to Adam Martin (and cited from [Wikipedia](https://en.wikipedia.org/wiki/Entity_component_system)):
```
 -   Entity: The entity is a general purpose object. Usually, it only consists of a unique id. They "tag every coarse gameobject as a separate item". Implementations typically use a plain integer for this.
 -   Component: the raw data for one aspect of the object, and how it interacts with the world. "Labels the Entity as possessing this particular aspect". Implementations typically use structs, classes, or associative arrays.
 -   System: "Each System runs continuously (as though each System had its own private thread) and performs global actions on every Entity that possesses a Component of the same aspect as that System."
```
### How ECS differs in Wave Engine?
 - Entity - contains ID and some way of deciding whether it should be active on any given stage
 - Component - database like mechanism, which provides the requestor with table-like structure on which it can iterate. 
 - System - Wave Engine contains 3 modes for updating each system - parallel to other systems, parallel before frame and never. Each system can do whatever it wants, however generally it gets "components" (the tables) and operates on them

## Features (or what is implemented)
 - ECS
 - multiple stages
 - multithreading (you might expect some hard to catch bugs)
 - simple graphics and text rendering
 
## Not yet implemented
 - sound system
 - bigger library of elements, including rendering anything other than simple image
 - others
 
 ## Examples
 In "example" folder there are simple examples which will be updated with WaveEngine development.
 
 ## Wave Engine and program flow 

Before Wave Engine starts, it needs to be of course configured. Configuration consists mainly of defining systems 
and creating entities. Then after engine is launched, each system is initialized via initialize(void) method.

Wave Engine contains two schedulers: SchedulerSingleThread and SchedulerMultiThread. Single threaded scheduler does everything
on one thread - it starts with normal systems, then renders graphics. Multi threaded scheduler runs everything on executor threads,
independently of each other - that is apart from systems that are defined as "Before Frame", which are always ran before frame is renderer.

To simplify the engine (from both usage and implementation perspective) all threads in both schedulers are synchronized. After synchronization
stage change can happen, which results in reloading of resources and changing active entities. However, this behaviour may be changed 
in the future - in particular splitting between normal systems and frame-update related ones.

For simplicity Notifying Service also runs on the thread that it was launched from.