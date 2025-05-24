const hre = require("hardhat");

async function main() {
  const QuestBadge = await hre.ethers.getContractFactory("QuestBadge");

  // Use the first signer from Hardhat (your PRIVATE_KEY account)
  const [deployer] = await hre.ethers.getSigners();

  // Pass deployer's address to the constructor
  const questBadge = await QuestBadge.deploy(deployer.address);

  await questBadge.waitForDeployment();
  console.log(`⛓️ Contract deployed to: ${questBadge.target}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
