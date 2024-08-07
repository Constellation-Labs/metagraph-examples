# Metagraph Example - Todo Data Application

## Overview
This template demonstrates a simple todo application built using the Constellation metagraph framework.

The following actions are supported:
- Create new tasks with status of [Backlog, InProgress, InReview] and assign a due date at least one hour from the time of creation
- Modify tasks to update the due date or update the status of the task
- Finalize tasks by marking [Complete, Close]

## Description
This example project is part of a video tutorial for developers learning to build on the Constellation Network. For the step-by-step guide please see the development walkthrough available at https://www.youtube.com/watch?v=mxWrxK_35Do. 

Note: The code commits referenced in the video are available on a [separate branch](https://github.com/Constellation-Labs/metagraph-examples/tree/todo-tutorial) of this repository with video timestamps and commits linked below. 
1. Initial setup and sensible defaults ([video](https://youtu.be/mxWrxK_35Do?t=820), [code](https://github.com/Constellation-Labs/metagraph-examples/commit/cab1087a15f81d7b387a7643d5b7661b362d7b23))
1. Schema design ([video](https://youtu.be/mxWrxK_35Do?t=1832), [code](https://github.com/Constellation-Labs/metagraph-examples/commit/672704b09037824362165526eb494266c7db8199))
1. Lifecycle methods
   1. State combiner ([video](https://youtu.be/mxWrxK_35Do?t=2337), [code](https://github.com/Constellation-Labs/metagraph-examples/commit/4d5bc82a8a51a125ac565769281c137c94d835b4))
   1. Validation rules ([video](https://youtu.be/mxWrxK_35Do?t=3166), [code](https://github.com/Constellation-Labs/metagraph-examples/commit/bedc911fd19bc17280dd5ba7f821123b1c56216b))
   1. Update validations ([video](https://youtu.be/mxWrxK_35Do?t=3502), [code](https://github.com/Constellation-Labs/metagraph-examples/commit/fb7c92ebf1e389b12e1619b9dd831c01222ff359))
1. Advanced topics
   1. Custom HTTP routes ([video](https://youtu.be/mxWrxK_35Do?t=3958), [code](https://github.com/Constellation-Labs/metagraph-examples/commit/6d4da251afac44cd23c8272148fb05981755bd6e))
   1. Daemons & Application configuration ([video](https://youtu.be/mxWrxK_35Do?t=4185), [code](https://github.com/Constellation-Labs/metagraph-examples/commit/481c898e9e64f1993368e1d1744dd89745adff67))
1. Testing ([video](https://youtu.be/mxWrxK_35Do?t=4356), [code](https://github.com/Constellation-Labs/metagraph-examples/commit/d496b3b25fed2e2f6467d3a443012e56d635dd73))

## Installation
In order to use the template version of this application it is recommended to install via the Euclid development environment with
```bash
./scripts/hydra install-template todo
```

This WILL NOT install the project commits referenced in the links above. To follow along with the video tutorial, clone this repository via
```bash
git clone git@github.com:Constellation-Labs/metagraph-examples.git
```
or use HTTPS if you have not previously authorized SSH keys. For information on SSH keys please consult the [GitHub docs](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/adding-a-new-ssh-key-to-your-github-account).
