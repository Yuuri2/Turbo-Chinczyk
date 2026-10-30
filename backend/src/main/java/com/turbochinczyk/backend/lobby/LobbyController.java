package com.turbochinczyk.backend.lobby;

import com.turbochinczyk.backend.Lobby;
import com.turbochinczyk.backend.database.AuthService;
import com.turbochinczyk.backend.database.UserSessionDto;
import com.turbochinczyk.backend.websocket.LobbyConnectionManager;
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
import org.springframework.web.socket.CloseStatus;

import java.util.List;

@RestController
@RequestMapping("/api/lobbies")
public class LobbyController {
    private final LobbyService lobbyService;
    private final AuthService authService;
    private final LobbyConnectionManager connectionManager;

    public LobbyController(
            LobbyService lobbyService,
            AuthService authService,
            LobbyConnectionManager connectionManager) {
        this.lobbyService = lobbyService;
        this.authService = authService;
        this.connectionManager = connectionManager;
    }

    @GetMapping
    public List<Lobby.LobbySnapshot> listLobbies() {
        return lobbyService.listJoinableLobbies();
    }

    @PostMapping
    public ResponseEntity<Lobby.LobbySnapshot> createLobby(
            @RequestBody CreateLobbyRequest request,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lobbyService.createLobby(
                        request.name(),
                        user,
                        request.password(),
                        request.maxPlayers()));
    }

    @GetMapping("/{lobbyId}")
    public Lobby.LobbySnapshot getLobby(@PathVariable int lobbyId) {
        try {
            return lobbyService.getLobby(lobbyId).snapshot();
        } catch (LobbyService.LobbyNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        }
    }

    @PostMapping("/{lobbyId}/join")
    public Lobby.LobbySnapshot joinLobby(
            @PathVariable int lobbyId,
            @RequestBody JoinLobbyRequest request,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        try {
            return lobbyService.joinLobby(
                    lobbyId,
                    user,
                    request.password());
        } catch (LobbyService.LobbyNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        } catch (LobbyService.IncorrectLobbyPasswordException exception) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Incorrect lobby password");
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    exception.getMessage(),
                    exception);
        }
    }

    @PostMapping("/{lobbyId}/leave")
    public void leaveLobby(
            @PathVariable int lobbyId,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        try {
            lobbyService.leaveLobby(lobbyId, user.getUserId());
        } catch (LobbyService.LobbyNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        } catch (LobbyService.LobbyMemberNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User is not a member of this lobby");
        }
    }

    @DeleteMapping("/{lobbyId}")
    public void deleteLobby(
            @PathVariable int lobbyId,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        try {
            lobbyService.deleteLobby(lobbyId, user.getUserId());
            connectionManager.closeLobby(
                    lobbyId,
                    new CloseStatus(4004, "Lobby was deleted"));
        } catch (LobbyService.LobbyNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        } catch (LobbyService.LobbyAccessDeniedException exception) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the host can delete the lobby");
        }
    }

    @PostMapping("/{lobbyId}/start")
    public Lobby.LobbySnapshot startGame(
            @PathVariable int lobbyId,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        try {
            return lobbyService.startGame(lobbyId, user.getUserId());
        } catch (LobbyService.LobbyNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    exception.getMessage(),
                    exception);
        }
    }

    @PostMapping("/{lobbyId}/ready")
    public Lobby.LobbySnapshot setReady(
            @PathVariable int lobbyId,
            @RequestBody ReadyRequest request,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        try {
            return lobbyService.setReady(
                    lobbyId,
                    user.getUserId(),
                    request.ready());
        } catch (LobbyService.LobbyNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    exception.getMessage(),
                    exception);
        }
    }

    @PostMapping("/{lobbyId}/finish")
    public Lobby.LobbySnapshot finishGame(
            @PathVariable int lobbyId,
            HttpServletRequest httpRequest) {
        UserSessionDto user = requireUser(httpRequest);
        try {
            return lobbyService.finishGame(lobbyId, user.getUserId());
        } catch (LobbyService.LobbyNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lobby does not exist");
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    exception.getMessage(),
                    exception);
        }
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
