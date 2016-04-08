## Build Scheduling Process

1. Creates BuildChain from payload (`BuildChainService` method __`createFromPayload()`__)
     1. Create build chain for logged in user
     2. Add the build options to the build chain
     3. Extract payload into a pre determined folder (based on build chain id)
     4. Parse build info (.apb.yml)
     5. Create one request for every `Platform` and `Flavor` combination found in the yml
        __Note__: if the `flavor` parameter is passed, only one request for the created flavor is created
     6. Populates all created Build Request with it's _flavor_, _platform_, _engine_ and _BuildChain_
     ---
     __Note:__ If any of the previous steps fails, a `CANCELLED` Build Request is created, and the process stops.
     
     ---
     
2. For each BuildRequest created, asynchronously schedule it ((`BuildRequestService` method __`schedule()`__))
    1. Find the actual __platform__ in the database that this Build Request should be built against
    2. Find the actual __flavor__ this request represents in the configuration file (`apb.yml`)
    3. Find the actual __engine__ in the database that this Build Request should be built against
    4. Validates that all the __required__ variables are passed
    5. Find a __BuildAgent__ capable of building this request.
        - __Note:__ This respects the build agent current load, if there are more than 1 build agent that can build the request, the less loaded will be chosen.
    6. Queue the request for execution on the BuildAgent.
    ---
    __Note:__ If any of the previous steps fails, the Build Request is cancelled.
    
    ---

3. Scheduling of the BuildRequest (starts on `BuildAgentQueueService` method __`queue()`__ called from the previous step)
    1. `BuildAgentQueueService` Finds or create a `BuildAgentQueueController` which is the component that manages the build agents queues.
    2. Submit the new task to the `BuildAgentQueueController`
    3. The BuildRequest is added to the queue BuildAgent queue.
    
    
## Build Execution Process

When a new `BuildAgentQueueController` is created (on demand based on the __Scheduling Process__ step _3.1_) to manage a Build Agent, the following steps are executed:

1. A __FIFO__ queue of `BuildRequest` is created with the capacity defined in the _BuildAgent_ `queue.size` variable or system default if not defined.
2. `BuildExecutorWorker`s are created respecting the `max.concurrent.builds` variable defined in the _BuildAgent_ or system default.
    - __NOTE:__ Currently only `SSHBuildExecutorWorker` are supported, but the architecture is enabled to support any kind of worker.
3. The `BuildExecutorWorker`s start watching the __FIFO__ queue and execute when a new `BuildRequest` arrives.`
4. When a Request Arrives ( __Scheduling Process__ step _3.3_) the execution begins
    1. The BuildRequest is marked as `RUNNING`and the `startTime` is populated.
    2. A new `Logger` is created to handle the build process logging.
    3. Steps when `SSHBuildExecutorWorker` is used 
        1. Connects to remote agent via `ssh`
        2. Create the necessary folders in the remote agent
        3. Uploads the compressed input file
        4. Extracts the compressed input file
        4. Builds the `BuildCommand` using the `BuildCommandBuilderService` (currently only docker commands are supported)
        5. Executes the `BuildCommand` on the agent, forwarding the output (stdout and stderr) to the build log.
        6. Tries to Downloads artifacts if the artifacts properties are populated.
        7. Finishes the request and disconnect from the agent.
        8. Cleanup the input files in the agent.
        9. Close any connected log consumers.
            __NOTE:__ If any error occurs that blocks the build, the build is marked as `FAILED`;
        ---

        
