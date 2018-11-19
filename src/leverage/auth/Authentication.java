package leverage.auth;

import leverage.Console;
import leverage.Kernel;
import leverage.auth.user.User;
import leverage.auth.user.UserProfile;
import leverage.auth.user.UserType;
import leverage.exceptions.AuthenticationException;
import leverage.util.Urls;
import leverage.util.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

// Sistema de Autenticacoin

public class Authentication {
    private final Console console;
    private final Set<User> userDatabase = new HashSet<>();
    private final Kernel kernel;
    private boolean authenticated;
    private String clientToken = UUID.randomUUID().toString();
    private User selectedAccount;

    public Authentication(Kernel k) {
        kernel = k;
        console = k.getConsole();
    }

    /**
     * Añade Usuario a la Configuracion
     * @param u The user to be added
     */
    private void addUser(User u) {
        if (userDatabase.contains(u)) {
            userDatabase.remove(u);
            userDatabase.add(u);
            console.print("Usuario " + u.getUserID() + " actualizado.");
        } else {
            userDatabase.add(u);
            console.print("Usuario " + u.getUserID() + " cargado.");
        }

    }

    /**
     * Remover Usuario a la Configuracion
     * @param u The User to be removed
     */
    public void removeUser(User u) {
        if (userDatabase.contains(u)) {
            console.print("Usuario " + u.getUserID() + " borrado.");
            userDatabase.remove(u);
            if (u.equals(selectedAccount)) {
                setSelectedUser(null);
            }
        } else {
            console.print("Usuario " + u.getUserID() + " no esta registrado.");
        }
    }

    /**
     * Devuele Usuaro Seleccionado
     * @return The selected user or null if no user is selected.
     */
    public final User getSelectedUser() {
        return selectedAccount;
    }

    /**
     * Selector de Usuarios
     * @param user The user to be selected
     */
    public void setSelectedUser(User user) {
        if (user != null) {
            console.print("Usuario " + user.getUserID() + " esta seleccionado ahora.");
        } else if (selectedAccount != null) {
            console.print("Usuario " + selectedAccount.getUserID() + " no esta seleccionado.");
        }
        selectedAccount = user;
    }

    /**
     * Sistema de Autenticacion
     * @param username The username
     * @param password The password
     * @throws AuthenticationException If authentication failed
     */
    public final void authenticate(String username, String password) throws AuthenticationException {
        console.print("Autenticando Usuario ...");
        // Inicializando Variables de Datos
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        UserType type;
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("agent", agent);
        String tmpUser;

        //Identificacion del Tipo de Logeo
        if (username.startsWith("leverage://")) {
            type = UserType.LEVERAGE;
            tmpUser = username.replace("leverage://", "");
        } else {
            type = UserType.OFFLINE;
            tmpUser = username;
        }

        // Cargando Informacion para Enviar al Servidor
        request.put("username", tmpUser);
        request.put("password", password);
        if (clientToken != null) {
            request.put("clientToken", clientToken);
        }
        request.put("accessToken", Utils.getUUID(tmpUser));
        request.put("requestUser", true);
        Map<String, String> postParams = new HashMap<>();
        postParams.put("Content-Type", "application/json; charset=utf-8");
        postParams.put("Content-Length", String.valueOf(request.toString().length()));
        String response;
        String authURL;

        // Autenticacion en Modo OFFLINE
        if (type == UserType.OFFLINE) {
            console.print("Autenticacion en modo OFFLINE");
            String uuid = Utils.getUUID(username);
            ArrayList<UserProfile> userProfiles = new ArrayList<>();
            UserProfile up = new UserProfile(uuid, username);
            userProfiles.add(up);

            User u = new User(uuid, clientToken, username, type, userProfiles, uuid);
            Kernel.USE_LOCAL = true;
            selectedAccount = u;
            authenticated = true;
            addUser(u);
        } else {
            // Autenticacion en Modo LEVERAGE
            console.print("Autenticacion en modo LEVERAGE");
            authURL = Urls.authPath;
            try {
                // Enviar Informacion en modo POST
                response = Utils.sendPost(authURL, request.toString().getBytes(Charset.forName("UTF-8")), postParams);
            } catch (IOException ex) {
                console.print("Ha Fallado la Comunicacion con el Servidor de Auteticacion");
                //ex.printStackTrace(console.getWriter());
                throw new AuthenticationException("Ha Fallado la Comunicacion con el Servidor de Auteticacion.");
            }
            if (response.isEmpty()) {
                throw new AuthenticationException("El Servidor de Auteticacion no ha respondido nada.");
            }
            JSONObject r;
            try {
                r = new JSONObject(response);
            } catch (JSONException ex) {
                throw new AuthenticationException("Ha fallado la lectura de Autenticacion.");
            }
            if (!r.has("error")) {
                try {
                    // Leyendo Respuesta del Servidor
                    String accessToken = r.getString("accessToken");
                    String selectedProfile = r.getJSONObject("selectedProfile").getString("id");
                    String userID = r.getJSONObject("user").getString("id");
                    clientToken = r.getString("clientToken");
                    ArrayList<UserProfile> userProfiles = new ArrayList<>();
                    JSONArray uprofs = r.getJSONArray("availableProfiles");
                    for (int i = 0; i < uprofs.length(); i++){
                        JSONObject prof = uprofs.getJSONObject(i);
                        UserProfile up = new UserProfile(prof.getString("id"), prof.getString("name"));
                        userProfiles.add(up);
                    }

                    //Añadiendo Usuario Autenticado
                    User u = new User(userID, accessToken, username, type, userProfiles, selectedProfile);
                    selectedAccount = u;
                    Kernel.USE_LOCAL = false;
                    authenticated = true;
                    addUser(u);
                } catch (JSONException ex) {
                    ex.printStackTrace(console.getWriter());
                    throw new AuthenticationException("Servidor de Autenticacion ha devuelto datos Errorneos!");
                }
            } else {
                authenticated = false;
                throwError(r);
            }
        }

    }

    /**
     * Refresca la Autenticacion, verificando si el Usuario ya esta Registrado
     * @throws AuthenticationException If the refresh failed
     */
    public final void refresh() throws AuthenticationException, JSONException {
        console.print("Refrescando Usuarios Logeados");
        if (selectedAccount == null) {
            throw new AuthenticationException("Usuario no Seleccionado.");
        }

        //Cargando Informacion de Logeo
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        User u = selectedAccount;
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("agent", agent);
        request.put("accessToken", u.getAccessToken());
        request.put("clientToken", clientToken);
        request.put("requestUser", true);
        Map<String, String> postParams = new HashMap<>();
        postParams.put("Content-Type", "application/json; charset=utf-8");
        postParams.put("Content-Length", String.valueOf(request.toString().length()));
        String response;
        String refreshURL;

        // Identificando Usuario OFFLINE
        if (u.getType() == UserType.OFFLINE) {
            Kernel.USE_LOCAL = true;
            authenticated = true;
            console.print("Autenticado Localmente.");
            return;
        } else {
            // Seleccionando URL para Refrescar la Autenticacion
            refreshURL = Urls.refreshPath;
        }
        try {
            // Enviar Informacion en modo POST
            response = Utils.sendPost(refreshURL, request.toString().getBytes(Charset.forName("UTF-8")), postParams);
        } catch (IOException ex) {
            authenticated = false;
            console.print("Ha Fallado la Comunicacion con el Servidor de Auteticacion.");
            throw new AuthenticationException("Ha Fallado la Comunicacion con el Servidor de Auteticacion.");
        }
        if (response.isEmpty()) {
            throw new AuthenticationException("El Servidor de Auteticacion no responde.");
        }
        JSONObject r;
        try {
            r = new JSONObject(response);
        } catch (JSONException ex) {
            throw new AuthenticationException("Ha fallado la lectura de datos del Servidor de Autenticacion.");
        }
        if (!r.has("error")) {
            try {
                //Revizar Informacion Recibida
                clientToken = r.getString("clientToken");
                u.setAccessToken(r.getString("accessToken"));
                String selectedProfile = r.getJSONObject("selectedProfile").getString("id");
                u.setSelectedProfile(selectedProfile);
                Kernel.USE_LOCAL = false;
                authenticated = true;
            } catch (JSONException ex) {
                ex.printStackTrace(console.getWriter());
                throw new AuthenticationException("Servidor de Autenticacion ha devuelto datos Errorneos!");
            }
        } else {
            authenticated = false;
            removeUser(selectedAccount);
            throwError(r);
        }
    }

    // Muestra Mensajes de Error de la Pagina al Usuario
    private void throwError(JSONObject message) throws AuthenticationException{
        if (message.has("errorMessage")) {
            throw new AuthenticationException(message.getString("errorMessage"));
        } else if (message.has("cause")) {
            throw new AuthenticationException(message.getString("error") + " caused by " + message.getString("cause"));
        } else {
            throw new AuthenticationException(message.getString("error"));
        }
    }

    /**
     * Chequea si el Usuario esta Autenticado
     * @return A boolean that indicates if is authenticated
     */
    public final boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Returns the client token
     * @return The client token
     */
    public final String getClientToken() {
        return clientToken;
    }

    /**
     * Cargar Usuarios desde launcher_profile.json
     */
    public final void fetchUsers() {
        console.print("Cargando Datos del Usuario.");
        JSONObject root = kernel.getLauncherProfiles();
        if (root != null) {
            String selectedUser = null;
            String selectedProfile = null;
            if (root.has("clientToken")) {
                clientToken = root.getString("clientToken");
            }
            if (root.has("selectedUser")) {
                Object selected = root.get("selectedUser");
                if (selected instanceof JSONObject) {
                    JSONObject s = (JSONObject)selected;
                    if (s.has("account")) {
                        selectedUser = s.getString("account");
                    }
                    if (s.has("profile")) {
                        selectedProfile = s.getString("profile");
                    }
                } else {
                    console.print("Legacy launcher_profiles.json found");
                }
            }
            if (root.has("authenticationDatabase")) {
                JSONObject users = root.getJSONObject("authenticationDatabase");
                Set s = users.keySet();
                for (Object value : s) {
                    String userID = value.toString();
                    JSONObject user = users.getJSONObject(userID);
                    if (user.has("accessToken") && user.has("username") && user.has("profiles")) {
                        String username = user.getString("username");
                        UserType userType = username.startsWith("leverage://") ? UserType.LEVERAGE : UserType.OFFLINE;
                        JSONObject profiles = user.getJSONObject("profiles");
                        Set profileSet = profiles.keySet();
                        if (profileSet.size() > 0) {
                            ArrayList<UserProfile> userProfiles = new ArrayList<>();
                            for (Object o : profileSet) {
                                String profileUUID = o.toString();
                                JSONObject profile = profiles.getJSONObject(profileUUID);
                                if (profile.has("displayName")) {
                                    UserProfile up = new UserProfile(profileUUID, profile.getString("displayName"));
                                    userProfiles.add(up);
                                }
                            }
                            User u;
                            if (userID.equalsIgnoreCase(selectedUser)) {
                                u = new User(userID, user.getString("accessToken"), username, userType, userProfiles, selectedProfile);
                                addUser(u);
                                setSelectedUser(u);
                            } else {
                                u = new User(userID, user.getString("accessToken"), username, userType, userProfiles, null);
                                addUser(u);
                            }
                        }
                    }
                }
            }
        } else {
            console.print("El Usuario no ha podido ser Cargado.");
        }
    }

    /**
     * Devuelve los Usuarios en la Configuracion
     * @return The user database
     */
    public final Set<User> getUsers() {
        return userDatabase;
    }

    /**
     * Convierte los Usuarios de la Configuracion a JSON
     * @return The user database in json format
     */
    public final JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("clientToken", clientToken);
        if (!userDatabase.isEmpty()) {
            JSONObject db = new JSONObject();
            for (User u : userDatabase) {
                JSONObject user = new JSONObject();
                user.put("accessToken", u.getAccessToken());
                user.put("username", u.getUsername());
                JSONObject profile = new JSONObject();
                for (UserProfile up : u.getProfiles()) {
                    JSONObject profileInfo = new JSONObject();
                    profileInfo.put("displayName", u.getDisplayName());
                    profile.put(up.getId(), profileInfo);
                }
                user.put("profiles", profile);
                db.put(u.getUserID(), user);
            }
            o.put("authenticationDatabase", db);
            JSONObject selectedUser = new JSONObject();
            if (selectedAccount != null) {
                selectedUser.put("account", selectedAccount.getUserID());
                selectedUser.put("profile", selectedAccount.getSelectedProfile());
            }
            o.put("selectedUser", selectedUser);
        }
        return o;
    }
}
