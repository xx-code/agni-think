import os
from fastapi import FastAPI
from core.dto import AgentPlanningAdivsorInput, AgentPlanningAdivsorOutput
from openIAAgent.agents import AgentPlanningAdvisor 

os.getenv("OPENAI_API_KEY")

app = FastAPI()


@app.get("/")
def read_root():
    return "Agni IA service"

@app.post("/agents/planning-advisor")
async def planning_advisor(input: AgentPlanningAdivsorInput) -> AgentPlanningAdivsorOutput:
    agent = AgentPlanningAdvisor()
    result = await agent.process(input)
    return result