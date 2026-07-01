# Graphics Design Editor

## Development Guidelines

### Code Style
- Java 11 target
- AndroidX libraries
- Annotations for null safety
- Comprehensive logging
- Thread-safe operations

### Performance
- Main thread: UI only
- Background threads: I/O and processing
- Monitor FPS and memory
- Detect and log jank

### Security
- Validate all inputs
- Encrypt sensitive data
- Use secure storage
- No cleartext traffic

### Testing
- Unit tests for managers
- Instrumentation tests for UI
- Performance benchmarks
- Memory leak detection

### Git Workflow
1. Create feature branch
2. Commit with descriptive messages
3. Push to remote
4. Create pull request
5. Code review and merge

### Documentation
- Code comments for complex logic
- Javadoc for public APIs
- README for setup
- CHANGELOG for updates
