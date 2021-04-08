# LinguaFrancaInGemoc

This website contains the formal semantics of [Lingua Franca](https://github.com/icyphy/lingua-franca/)
 defined by using the [MoCCML approach](http://timesquare.inria.fr/moccml/) in the [Gemoc studio](http://eclipse.org/gemoc).

This is still a work in progess but it is already possible to debug LF models like shown in the following screenshot:

![LFdebugging](/screenshots/scatterGatherDebug.png)

It is also possible to do some exhaustive simulation to explore all the state space of a LF program. Resulting state spaces are amenable to model checking in the CADP tool:

![LFstateSpace](/screenshots/scatterGatherStateSpace.png)

## Videos

 - [video of slides](https://unice-my.sharepoint.com/:v:/g/personal/julien_deantoni_unice_fr/EZIZL5ROok1DupbGhVNspKgB8d2mrKEFbTLkXeam6s0Lsg?e=1KDpUK) explaining what I understood so far about the concurrent and timed operational semantics of LF, as weel as few explanations on how it is implemented
 - [Demo of the omniscient debugger (video)](https://unice-my.sharepoint.com/:v:/g/personal/julien_deantoni_unice_fr/ESTC5OgpnMdKsWne9hfnS6sB6JTRb26DjIEF4KqidfzfVQ?e=8BaRHq) for LF, allowing to navigate forward and backward in time
 - [Demo of the check of assertions (video)](https://unice-my.sharepoint.com/:v:/g/personal/julien_deantoni_unice_fr/ESbXfVCpMFJEu9k0cWPm6n8BQWzOjyShwCNkjY7tKQQxsA?e=tY8T66) for a specific LF program. Assertions are written in CCSL
 - [Demo of model checking  (video)](https://unice-my.sharepoint.com/:v:/g/personal/julien_deantoni_unice_fr/EQGaYGG9q_RMmJYawCy7eRoBdjiuOw7LnWWkFQN5v5S-bQ?e=kcaIV6) LF program and injection of the counter example back into the debugging environment


## Notes

this work is based on this fork of the LF tooling: https://github.com/jdeantoni/lingua-franca
