

interface Good {
    mode: number;
    last: number;
    stock: number;
    dur: number;
    prev: number;
    val: number;
    vals: number[];
    d: number;
    id: number;
    sum?: number;
}

const goodsById: Good[] = [];

import { readFileSync } from 'fs';
import JSON5 from 'json5';

// Load configuration from JSON5 file (supports comments)
const config = JSON5.parse(readFileSync('../../config.jsonc', 'utf-8'));
const bankLevel = config.bankLevel;                           // Bank upgrade level
const dragonBoost = config.hasSupremeIntellect ? 1 : 0;      // Supreme Intellect dragon upgrade



/** Initializes the game with the code from the game */
function gameInit() {

    var globD = 0; var globP = Math.random();



    function getRestingVal(id) {
        return 10 + 10 * id + (bankLevel - 1);
    }
    function choose(arr) { return arr[Math.floor(Math.random() * arr.length)]; }

    for (let i = 0; i < 18; i++) {
        let it = {
            mode: 0,
            last: 0,
            stock: 0,
            dur: 0,
            prev: 0,
            val: getRestingVal(i),
            vals: [getRestingVal(i)],
            d: 0,
            id: i,
        };
        goodsById.push(it);
    }


    function tick() {
        if (Math.random() < 0.1 + 0.1 * dragonBoost) globD = (Math.random() - 0.5) * 2;
        for (var i = 0; i < goodsById.length; i++) {
            var me = goodsById[i];
            me.last = 0;

            me.d *= 0.97 + 0.01 * dragonBoost;

            if (me.mode == 0) { me.d *= 0.95; me.d += 0.05 * (Math.random() - 0.5); }
            else if (me.mode == 1) { me.d *= 0.99; me.d += 0.05 * (Math.random() - 0.1); }
            else if (me.mode == 2) { me.d *= 0.99; me.d -= 0.05 * (Math.random() - 0.1); }
            else if (me.mode == 3) { me.d += 0.15 * (Math.random() - 0.1); me.val += Math.random() * 5; }
            else if (me.mode == 4) { me.d -= 0.15 * (Math.random() - 0.1); me.val -= Math.random() * 5; }
            else if (me.mode == 5) me.d += 0.3 * (Math.random() - 0.5);

            me.val += (getRestingVal(me.id) - me.val) * 0.01;

            if (globD != 0 && Math.random() < globP) { me.val -= (1 + me.d * Math.pow(Math.random(), 3) * 7) * globD; me.val -= globD * (1 + Math.pow(Math.random(), 3) * 7); me.d += globD * (1 + Math.random() * 4); me.dur = 0; }

            me.val += Math.pow((Math.random() - 0.5) * 2, 11) * 3;
            me.d += 0.1 * (Math.random() - 0.5);
            if (Math.random() < 0.15) me.val += (Math.random() - 0.5) * 3;
            if (Math.random() < 0.03) me.val += (Math.random() - 0.5) * (10 + 10 * dragonBoost);
            if (Math.random() < 0.1) me.d += (Math.random() - 0.5) * (0.3 + 0.2 * dragonBoost);
            if (me.mode == 5) {
                if (Math.random() < 0.5) me.val += (Math.random() - 0.5) * 10;
                if (Math.random() < 0.2) me.d = (Math.random() - 0.5) * (2 + 6 * dragonBoost);
            }
            if (me.mode == 3 && Math.random() < 0.3) { me.d += (Math.random() - 0.5) * 0.1; me.val += (Math.random() - 0.7) * 10; }
            if (me.mode == 3 && Math.random() < 0.03) { me.mode = 4; }
            if (me.mode == 4 && Math.random() < 0.3) { me.d += (Math.random() - 0.5) * 0.1; me.val += (Math.random() - 0.3) * 10; }

            if (me.val > (100 + (bankLevel - 1) * 3) && me.d > 0) me.d *= 0.9;

            me.val += me.d;
            /*if (me.val<=0 && me.d<0)
            {
                me.d*=0.75;
                if (me.mode==4 && Math.random()<0.05) me.mode=2;
            }
            if (me.val<2) me.val+=(2-me.val)*0.1;
            me.val=Math.max(me.val,0.01);*/
            /*var cutoff=5;
            var minvalue=1;
            if (me.val<=cutoff)
            {
                var s=Math.max(0,me.val)/cutoff;
                me.val=((2*minvalue-cutoff)*s+(2*cutoff-3*minvalue))*s*s+minvalue;//low soft-cap between 1 and 5
            }*/
            if (me.val < 5) me.val += (5 - me.val) * 0.5;
            if (me.val < 5 && me.d < 0) me.d *= 0.95;
            me.val = Math.max(me.val, 1);
            me.vals.push(me.val);
            me.dur--;
            //if (Math.random()<1/me.dur)
            if (me.dur <= 0) {
                me.dur = Math.floor(10 + Math.random() * (690 - 200 * dragonBoost));
                if (Math.random() < dragonBoost && Math.random() < 0.5) me.mode = 5;
                else if (Math.random() < 0.7 && (me.mode == 3 || me.mode == 4)) me.mode = 5;
                else me.mode = choose([0, 1, 1, 2, 2, 3, 4, 5]);
            }
        }
        // M.checkGraphScale();
        // M.toRedraw = Math.max(M.toRedraw, 1);
        // M.ticks++;
    }

    return tick
}


const tick = gameInit();

// How many years to simulate
const YEARS = 1;

const TICKS_PER_YEAR = 60 * 24 * 365; // = 525600
const TICKS = TICKS_PER_YEAR * YEARS;

let i = 0;
let last = Date.now()
for (; i < TICKS; i++) {
    if (i % TICKS_PER_YEAR == 0) {
        let now = Date.now();
        let yearsLeft = YEARS - i / TICKS_PER_YEAR;
        let timePerYear = (now - last);
        let ETA = timePerYear * yearsLeft;
        let end = new Date(now + ETA);
        let timeRemaining = new Date(ETA);
        console.log(`ETA: \x1b[1m${timeRemaining.getUTCHours().toString().padStart(2, '0')}:${timeRemaining.getUTCMinutes().toString().padStart(2, '0')}:${timeRemaining.getUTCSeconds().toString().padStart(2, '0')}\x1b[0m - at \x1b[1m${end.toLocaleTimeString()}\x1b[0m`);
        console.log((i / TICKS_PER_YEAR).toString() + " years simulated");
        last = now;
    }
    tick();
}

import * as d3 from 'd3-array';

/** Bold and format number */
function b(n: number) {
    return `\x1b[1m${n.toFixed(0)}\x1b[0m`;
}

let text = ""

for (let good of goodsById) {
    const quantiles = [
        0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95, 0.99
    ]
    const values = good.vals;
    // Sort values
    const sorted = d3.sort(values);
    // Calculate quantiles
    const quantileValues = quantiles.map(q => d3.quantileSorted(sorted, q));

    text += quantileValues.join(" ") + "\n";

}


// Write text to file
import { writeFileSync } from 'fs';
writeFileSync('simulation_results.txt', text);

