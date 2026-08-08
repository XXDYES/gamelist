package org.example.gamelist.client;

import org.example.gamelist.dto.GameInfoDTO;

public interface AiClient {
    GameInfoDTO getGameInfo(String gamename);
}
