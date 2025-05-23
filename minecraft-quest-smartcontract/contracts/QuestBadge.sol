// SPDX-License-Identifier: MIT
pragma solidity ^0.8.18;

import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract QuestBadge is ERC721URIStorage, Ownable {
    uint256 public nextTokenId;
    mapping(address => bool) public hasMinted;

    constructor() ERC721("QuestBadge", "QBDG") Ownable(msg.sender) {}

    function mintBadge(address player, string memory tokenURI) public onlyOwner {
        require(!hasMinted[player], "Player already received badge");
        uint256 tokenId = nextTokenId;
        _safeMint(player, tokenId);
        _setTokenURI(tokenId, tokenURI);
        hasMinted[player] = true;
        nextTokenId++;
    }
}
