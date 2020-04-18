
# Wave Engine
**Wave Engine** is easy to use, ECS-based, 2D, multithreaded, Java game engine. Currently it is extremely WIP and underdocumented.
  
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
 - Component - database like mechanism, which provides requestor with table-like structure on which it can iterate. 
 - System - Wave Engine contains has 3 modes for updating each system - parallel to other systems (updated according to given Updates Per Second), parallel after frame (updated according to given FPS, after rendering frame) and never. Each system can do whatever it wants, however generally it gets "components" and operates on them

## Features (or what is implemented)
 - ECS
 - multiple stages
 - multithreading (you might expect some hard to catch bugs)
 - simple graphics and text rendering
 
## Not yet implemented
 - sound system
 - automatic cleanup of not used resources
 - bigger library of elements, including rendering anything other than simple image
 - others
 
 ## Examples
 In "example" folder there are simple examples which will be updated with WaveEngine development.