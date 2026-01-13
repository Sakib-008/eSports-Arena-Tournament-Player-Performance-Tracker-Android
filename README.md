# Esports Arena

An Android application for managing esports players, teams, tournaments, and competitive matches. This app provides a comprehensive platform for player statistics tracking, leaderboards, match management, and tournament organization.

## Features

- **User Authentication**: Login and signup system for players
- **Player Profiles**: Detailed player information including statistics, teams, and availability
- **Player Dashboard**: Personal dashboard for players to view their stats and performance
- **Team Management**: View and manage player teams
- **Leaderboard**: Dynamic leaderboards with player rankings
- **Player Voting**: Community voting system for player recognition (Leader Vote)
- **Match Management**: Track upcoming matches and results
- **Tournament System**: Organize and manage tournaments with associated statistics
- **Statistics Tracking**: Track kills, deaths, assists, win/loss records
- **Real-time Data**: Firebase Realtime Database integration for live data synchronization

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/esports_arena/
│   │   ├── HomeActivity.java                  # Entry point/splash screen
│   │   ├── LoginActivity.java                 # User login
│   │   ├── SignupActivity.java                # User registration
│   │   ├── PlayerDashboardActivity.java       # Player personal dashboard
│   │   ├── PlayerProfileActivity.java         # Player profile view
│   │   ├── LeaderVoteActivity.java            # Player voting interface
│   │   ├── LeaderboardFragment.java           # Leaderboard display
│   │   ├── PlayerProfileFragment.java         # Player profile fragment
│   │   ├── PlayerStatsFragment.java           # Player statistics display
│   │   ├── TeamFragment.java                  # Team management
│   │   ├── UpcomingMatchesFragment.java       # Upcoming matches display
│   │   ├── model/                             # Data models
│   │   │   ├── Player.java                    # Player model
│   │   │   ├── Team.java                      # Team model
│   │   │   ├── Match.java                     # Match model
│   │   │   ├── Tournament.java                # Tournament model
│   │   │   ├── TournamentStats.java           # Tournament statistics
│   │   │   ├── PlayerMatchStats.java          # Per-match player stats
│   │   │   └── LeaderVote.java                # Voting model
│   │   ├── data/                              # Data access layer
│   │   │   ├── FirebaseService.java           # Firebase service utilities
│   │   │   ├── PlayerRepository.java          # Player data access
│   │   │   ├── TeamRepository.java            # Team data access
│   │   │   ├── MatchRepository.java           # Match data access
│   │   │   ├── TournamentRepository.java      # Tournament data access
│   │   │   └── LeaderVoteRepository.java      # Vote data access
│   │   └── service/
│   │       └── TournamentStatsService.java    # Tournament stats processing
│   └── res/                                   # Resources (layouts, drawables, values)
└── google-services.json                       # Firebase configuration
```

## Key Components

### Activities
- **HomeActivity**: Main entry point with login/signup buttons
- **LoginActivity**: User authentication interface
- **SignupActivity**: New user registration
- **PlayerDashboardActivity**: Personal player dashboard with statistics
- **PlayerProfileActivity**: Detailed player profile view
- **LeaderVoteActivity**: Vote for top players

### Fragments
- **LeaderboardFragment**: Display ranked player leaderboards
- **PlayerProfileFragment**: Player profile details
- **PlayerStatsFragment**: In-depth statistics display
- **TeamFragment**: Team information and management
- **UpcomingMatchesFragment**: Display upcoming matches

### Data Models
- **Player**: User account with stats (kills, deaths, assists, wins, losses)
- **Team**: Team information and members
- **Match**: Match details and outcomes
- **Tournament**: Tournament information and structure
- **TournamentStats**: Per-player tournament statistics
- **PlayerMatchStats**: Detailed performance metrics per match
- **LeaderVote**: Player voting/rating system

## Technologies Used

### Android Framework
- **MinSDK**: 24 (Android 7.0)
- **TargetSDK**: 36 (Android 14)
- **Compile SDK**: 36 (Android 14)
- Java 11 compatibility

### Libraries & Dependencies
- **AndroidX**: AppCompat, Material, ConstraintLayout, RecyclerView
- **Firebase**: Realtime Database, Analytics, Google Services
- **MPAndroidChart**: v3.1.0 (Data visualization for statistics)

### Architecture
- Repository pattern for data access
- Firebase Realtime Database for backend
- Activity/Fragment-based UI navigation

## Build Information

- **Application ID**: com.example.esports_arena
- **Version Code**: 1
- **Version Name**: 1.0
- **Build System**: Gradle with Kotlin DSL

## Setup & Installation

### Prerequisites
- Android Studio (latest version recommended)
- JDK 11 or higher
- Android SDK 36

### Installation Steps

1. Clone or download the project:
   ```bash
   git clone <repository-url>
   cd esports-arena
   ```

2. Configure Firebase:
   - Place your `google-services.json` file in the `app/` directory
   - Ensure Firebase Realtime Database is configured

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. Run the app:
   - Open in Android Studio and click **Run** or use:
   ```bash
   ./gradlew installDebug
   ```

## Permissions

The app requires the following permissions:
- **INTERNET**: For Firebase communication and API calls

## Database Structure

The app uses Firebase Realtime Database with the following main nodes:
- `players`: Player account and profile data
- `teams`: Team information
- `matches`: Match records
- `tournaments`: Tournament data
- `leaderVotes`: Player voting data

## Testing

The project includes:
- JUnit for unit testing
- Espresso for UI testing

Run tests with:
```bash
./gradlew test          # Unit tests
./gradlew connectedAndroidTest  # UI tests
```

## Contributing

This is a course project for CSE 2200 (Advanced Programming Laboratory) at Khulna University of Engineering and Technology (KUET). 

## Notes

- This is a development version (v1.0)
- ProGuard/R8 code shrinking is disabled for debug builds
- The app uses edge-to-edge displays for immersive UI experience
- Real-time synchronization is powered by Firebase

---

**Course**: Advanced Programming Laboratory (CSE 2200)  
**Project Type**: Android Application  
**Last Updated**: January 2026
