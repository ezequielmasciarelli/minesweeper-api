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
    minefield.classList.add(`${xPos}${yPos}`);
    const myXpos = xPos;
    const myYpos = yPos;
    minefield.addEventListener("click",() => {
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