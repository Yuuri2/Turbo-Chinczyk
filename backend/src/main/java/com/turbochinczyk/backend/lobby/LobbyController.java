package com.turbochinczyk.backend.lobby;

import com.turbochinczyk.backend.Lobby;
import com.turbochinczyk.backend.LobbyManager;
import com.turbochinczyk.backend.database.AuthService;
import com.turbochinczyk.backend.database.UserSessionDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/lobbies")
public class LobbyController {
    private final LobbyManager lobbyManager;
    private final AuthService authService;

    public LobbyController(LobbyManager lobbyManager, AuthService authService) {
        this.lobbyManager = lobbyManager;
        this.authService = authService;
    }

    @GetMapping
    public List<Lobby.LobbySnapshot> listLobbies() {
        return lobbyManager.listLobbies()
                .stream()
                .filter(Lobby::isJoinable)
                .map(Lobby::snapshot)
                .toList();
    }

    @PostMapping
    public ResponseEntity<Lobby.LobbySnapshot> createLobby(
            @RequestBody CreateLobbyRequest request,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);

        Lobby lobby = lobbyManager.createLobby(
                request.name(),
                user.getUserId(),
                request.password(),
                request.maxPlayers());

        lobby.addMember(user.getUserId(), user.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lobby.snapshot());
    }

    @GetMapping("/{lobbyId}")
    public Lobby.LobbySnapshot getLobby(@PathVariable String lobbyId) {
        Lobby lobby = lobbyManager.get(lobbyId);
        if (lobby == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        }

        return lobby.snapshot();
    }

    @PostMapping("/{lobbyId}/join")
    public Lobby.LobbySnapshot joinLobby(
            @PathVariable String lobbyId,
            @RequestBody JoinLobbyRequest request,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        Lobby lobby = lobbyManager.get(lobbyId);

        if (lobby == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        }

        if (!lobby.hasCorrectPassword(request.password())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Incorrect lobby password");
        }

        try {
            lobby.addMember(user.getUserId(), user.getUsername());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    exception.getMessage(),
                    exception);
        }

        return lobby.snapshot();
    }

    @PostMapping("/{lobbyId}/leave")
    public void leaveLobby(
            @PathVariable String lobbyId,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        if (lobbyManager.get(lobbyId) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        }
        if (!lobbyManager.removeMember(lobbyId, user.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User is not a member of this lobby");
        }
    }

    @DeleteMapping("/{lobbyId}")
    public void deleteLobby(
            @PathVariable String lobbyId,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        LobbyManager.DeleteLobbyResult result =
                lobbyManager.deleteLobby(lobbyId, user.getUserId());

        if (result == LobbyManager.DeleteLobbyResult.NOT_FOUND) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        }
        if (result == LobbyManager.DeleteLobbyResult.NOT_HOST) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the host can delete the lobby");
        }
    }

    @PostMapping("/{lobbyId}/start")
    public Lobby.LobbySnapshot startGame(
            @PathVariable String lobbyId,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        LobbyManager.LobbyStatusResult result =
                lobbyManager.startGame(lobbyId, user.getUserId());
        if (result == LobbyManager.LobbyStatusResult.NOT_FOUND) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        }
        if (result == LobbyManager.LobbyStatusResult.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lobby cannot be started by this request");
        }
        return lobbyManager.get(lobbyId).snapshot();
    }

    @PostMapping("/{lobbyId}/ready")
    public Lobby.LobbySnapshot setReady(
            @PathVariable String lobbyId,
            @RequestBody ReadyRequest request,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        LobbyManager.LobbyStatusResult result =
                lobbyManager.setPlayerReady(
                        lobbyId,
                        user.getUserId(),
                        request.ready());
        if (result == LobbyManager.LobbyStatusResult.NOT_FOUND) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        }
        if (result == LobbyManager.LobbyStatusResult.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Player readiness cannot be changed");
        }
        return lobbyManager.get(lobbyId).snapshot();
    }

    @PostMapping("/{lobbyId}/finish")
    public Lobby.LobbySnapshot finishGame(
            @PathVariable String lobbyId,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        LobbyManager.LobbyStatusResult result =
                lobbyManager.finishGame(lobbyId, user.getUserId());
        if (result == LobbyManager.LobbyStatusResult.NOT_FOUND) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        }
        if (result == LobbyManager.LobbyStatusResult.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lobby is not currently playing");
        }
        return lobbyManager.get(lobbyId).snapshot();
    }

    private UserSessionDto requireUser(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required");
        }

        for (Cookie cookie : request.getCookies()) {
            if ("session".equals(cookie.getName())) {
                return authService.validateToken(cookie.getValue())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authentication is required"));
            }
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authentication is required");
    }

    public record CreateLobbyRequest(
            String name,
            String password,
            int maxPlayers) {
    }

    public record JoinLobbyRequest(String password) {
    }

    public record ReadyRequest(boolean ready) {
    }
}
