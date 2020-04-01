const times = x => f => {
  if (x > 0) {
    f();
    times (x - 1) (f)
  }
};


const main = document.getElementsByClassName("main")[0];
let xPos = 0;
let yPos = 0;
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
    const myXpos = xPos;
    const myYpos = yPos;
    minefield.addEventListener("click",(event) => {
      const body = {
        "xPos" : myXpos,
        "yPos" : myYpos
      };
      fetch(`http://localhost:9000/click`, {
        method: 'POST',
        body: JSON.stringify(body),
        headers:{
          'Content-Type': 'application/json'
        }
      })
          .then(res => res.json())
          .then(res => {
            if(res.alive){
              const element = event.target;
              element.classList.add("clicked");
              element.innerHTML = res.currentMine.neighborsWithMines;
              res.neighborsDiscovered.forEach(mineDiscovered => {
                const xPos = mineDiscovered.coordinates[0];
                const yPos = mineDiscovered.coordinates[1];
                const neighbor = document.getElementById(`${xPos}${yPos}`);
                neighbor.classList.add("clicked");
                neighbor.innerHTML = mineDiscovered.neighborsWithMines;
              });
              console.log("Sigue vivo")
            }
            else{

              console.log("Murio")
            }
          })
    });
    row.appendChild(minefield);
    xPos++;
  });
  main.appendChild(row);
  yPos++;
});