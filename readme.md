
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
 - System - Wave Engine contains 2 modes for updating each system - parallel to other systems and never. Each system can do whatever it wants, however generally it gets "components" (the tables) and operates on them

## Features (or what is implemented)
 - ECS
 - multiple stages
 - multithreading (you might expect some hard to catch bugs)
 - simple graphics and text rendering
 
## Not yet implemented
 - sound system
 - bigger library of elements, including rendering anything other than simple image
 - optimized component manager
 - faster rendering (OpenGL might be needed)
 - networking
 - saving
 - others
 
 ## Examples
 In "example" folder there are simple examples which will be updated with WaveEngine development. They show usage of the 
 API and should be understandable enough for most programmers.
 
 ## Wave Engine and program flow 

Before Wave Engine starts, it needs to be of course configured. Configuration consists mainly of defining systems 
and creating entities. Then after the engine is launched, each system is initialized via initialize(void) method.

Wave Engine contains two schedulers: SchedulerSingleThread and SchedulerMultiThread. Single threaded scheduler does everything
on one thread - it starts with normal systems, then renders graphics. Multi threaded scheduler runs everything on executor threads,
independently of each other.

For good results you should assume that all systems can work in any order. If you want some systems to work in strictly defined order, you should instead
define those two (or more) systems as one.

However, in reality in single threaded scheduler everything will generally work in the same order always. This behaviour might change in future. 
In multithreaded scheduler there is no such guarantee. With multithreaded scheduler there is additional synchronization that happens when there
is component update, and all systems must stop work then. Although at the time of writing of this comment, systems that are not updated
by scheduler are not stopped if they access component manager, but they should.

For simplicity Notifying Service also runs on the thread that it was launched from.

All systems should be considered to be timely independent of others. That means, that if one of the systems lags behind, other systems
do not wait for it. Keep in mind that it also means that slow systems will delay stage change (and addition of new entities), and other systems might wait for data from other systems.

##Other things to keep in mind
 - passing a null pointer to method for no reason is a bad idea
 - adding or removing entities at stage runtime might get expensive as it will lead to blocks and index rebuilding - if you use
 particle systems, consider having them in a separate entity (composite)
 - allocating massive amounts of new objects in each iteration might get expensive in JVM. Consider reusing objects (like in basic example
 parameters for renderer are reused in **per object** basis) 

