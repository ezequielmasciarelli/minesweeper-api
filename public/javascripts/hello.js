async function init() {
  const code = await fetch("http://localhost:9000/new")
      .then(res => res.status);
  if(code === 200) {
    console.log(`New Game started correctly`)
  } else {
    console.log(`Error starting game. Code ${code}`)
  }
}

init();

const times = x => f => {
  if (x > 0) {
    f();
    times (x - 1) (f)
  }
};


const main = document.getElementsByClassName("main")[0];
let xPos = 0;
let yPos = 0;
let elements = [];
times(10)(() => {
  xPos=0;
  const row = document.createElement("div");
  row.classList.add("flex");
  times(10)(() => {
    const minefield = document.createElement("div");
    minefield.classList.add("square");
    minefield.classList.add("mine");
    minefield.id = `${xPos}${yPos}`;
    minefield.classList.add(`${xPos}${yPos}`);
    const body = {
      "xPos" : xPos,
      "yPos" : yPos
    };
    minefield.addEventListener("click",(event) => checkMine(event,body));
    row.appendChild(minefield);
    elements.push(minefield);
    xPos++;
  });
  main.appendChild(row);
  yPos++;
});

function checkMine(event,body) {
  fetch(`http://localhost:9000/click`, {
    method: 'POST',
    body: JSON.stringify(body),
    headers:{
      'Content-Type': 'application/json'
    }
  })
      .then(res => res.json())
      .then(res => {
        const element = event.target;
        if(res.alive){
          element.classList.add("clicked");
          element.innerHTML = res.currentMine.neighborsWithMines;
          res.neighborsDiscovered.forEach(mineDiscovered => {
            const xPos = mineDiscovered.coordinates[0];
            const yPos = mineDiscovered.coordinates[1];
            const neighbor = document.getElementById(`${xPos}${yPos}`);
            neighbor.classList.add("clicked");
            neighbor.innerHTML = mineDiscovered.neighborsWithMines;
          });
          console.log("Sigue vivo");
        }
        else{
          console.log("Murio");
          element.classList.add("dead");
          element.innerHTML = "X";
          const gameover = document.createElement("div");
          const notificationDiv = document.createElement("div");
          notificationDiv.innerHTML = "Game Over";
          gameover.classList.add("gameover");
          notificationDiv.classList.add("notification");
          gameover.appendChild(notificationDiv);
          main.appendChild(gameover)
        }
      })
}