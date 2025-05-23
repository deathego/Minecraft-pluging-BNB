const hre = require("hardhat");

async function main() {
  const QuestBadge = await hre.ethers.getContractFactory("QuestBadge");
  const questBadge = await QuestBadge.deploy();
  await questBadge.waitForDeployment(); 

  console.log(`Contract deployed to: ${questBadge.target}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
