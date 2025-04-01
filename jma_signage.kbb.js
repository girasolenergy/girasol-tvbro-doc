// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.jma_signage",
//     "title": "JMA Signage",
//     "version": "0.0.1",
//     "description": "This plugin customizes the JMA map for signage display."
// }

if (location.href.includes('jma.go.jp')) {
  new Promise(async () => {
    async function getById(id) {
      for (let i = 0; i < 50; i++) {
        const element = document.getElementById(id);
        if (element != null) return element;
        await new Promise(resolve => setTimeout(resolve, 200));
      }
      throw new Error(`No such element: #${id}`)
    }
  
    console.log("[jma_signage] play");
    (await getById("unitmap-playbutton")).click();
    
    console.log("[jma_signage] set play speed");
    (await getById("unitmap-tacobutton")).click();
    (await getById("unitmap-tacobutton")).click();
    
    console.log("[jma_signage] hide timeslider");
    (await getById("unitmap-timeslider")).style.display = "none";
    
    console.log("[jma_signage] hide header");
    (await getById("unitmap-header")).style.display = "none";
    
    console.log("[jma_signage] hide control container");
    Array.from(document.querySelectorAll(".leaflet-control-container")).forEach(e => e.style.display = "none");
    
    console.log("[jma_signage] hide information space");
    Array.from(document.querySelectorAll(".unitmap-information-space")).forEach(e => e.style.display = "none");
    
    console.log("[jma_signage] hide switches");
    (await getById("unitmap-switches")).style.display = "none";
    
    console.log("[jma_signage] hide unitmap");
    (await getById("unitmap")).style.top = "0";
    
    console.log("[jma_signage] finish");
  });
}
