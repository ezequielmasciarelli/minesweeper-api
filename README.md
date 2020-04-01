# minesweeper-API
Here is a simple minesweeper API that allows you to play minesweeper in a RESTFull Way.
It consists in 2 main endpoints:
1) GET /new => Starts a new Game. The game is allways of size 10x10 with 20 mines (it is easy to scale due to the way the map is created at the backend, but the frontend would need mayor changes)

2) POST /click => Sends at the body the X and Y coordinates and check if there is a mine inside. If there isn't, returns a 200 with the boolean alive:true and a LIST of the discovered places. Thats because if you click a mine that all their neighbors doens't have mines neither they will became "discovered" too. This was made in a recursive way at the backend

NOTES: The game was made purely in SCALA and trying to make everything most immutable as possible. The only thing that is not immutable in the map is the recursive algorithm that check the discovered mines (it was done like a graph search).
The map is declared as VAR, but the list inside is immutable too.
The framework used for this application is a PLAY, and below are the realized parts of the challenge:


The frontend application is served as a static from this same application, and shows a basic example of the game flow and how you can interact with the game. Is was done purely in JS without any library.

---------------------------------------------

API test

We ask that you complete the following challenge to evaluate your development skills. Please use the programming language and framework discussed during your interview to accomplish the following task.

PLEASE DO NOT FORK THE REPOSITORY. WE NEED A PUBLIC REPOSITORY FOR THE REVIEW. 

## The Game
Develop the classic game of [Minesweeper](https://en.wikipedia.org/wiki/Minesweeper_(video_game))

## Show your work

1.  Create a Public repository ( please dont make a pull request, clone the private repository and create a new plublic one on your profile)
2.  Commit each step of your process so we can follow your thought process.

## What to build
The following is a list of items (prioritized from most important to least important) we wish to see:
* Design and implement  a documented RESTful API for the game (think of a mobile app for your API)
* Implement an API client library for the API designed above. Ideally, in a different language, of your preference, to the one used for the API
* When a cell with no adjacent mines is revealed, all adjacent squares will be revealed (and repeat)
* Ability to 'flag' a cell with a question mark or red flag
* Detect when game is over
* Persistence
* Time tracking
* Ability to start a new game and preserve/resume the old ones
* Ability to select the game parameters: number of rows, columns, and mines
* Ability to support multiple users/accounts
 
## Deliverables we expect:
* URL where the game can be accessed and played (use any platform of your preference: heroku.com, aws.amazon.com, etc)
* Code in a public Github repo
* README file with the decisions taken and important notes

## Time Spent
You do not need to fully complete the challenge. We suggest not to spend more than 5 hours total, which can be done over the course of 2 days.  Please make commits as often as possible so we can see the time you spent and please do not make one commit.  We will evaluate the code and time spent.
 
What we want to see is how well you handle yourself given the time you spend on the problem, how you think, and how you prioritize when time is insufficient to solve everything.

Please email your solution as soon as you have completed the challenge or the time is up.
