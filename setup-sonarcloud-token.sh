#!/bin/bash

# SonarCloud Token Setup Script
# This script helps automate the SonarCloud token configuration process

set -e

echo "🔧 SonarCloud Token Setup Script"
echo "================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if SONAR_TOKEN is already set
if [ ! -z "$SONAR_TOKEN" ]; then
    print_success "SONAR_TOKEN is already set in environment"
    echo "Token value: ${SONAR_TOKEN:0:10}..."
    echo ""
else
    print_warning "SONAR_TOKEN is not set in environment"
    echo ""
fi

# Instructions for manual token creation
print_info "To create a SonarCloud token manually:"
echo "1. 🌐 Visit: https://sonarcloud.io/account/security/"
echo "2. 🔑 Click 'Generate Tokens'"
echo "3. 📝 Enter token name: 'wanli-backend-cli'"
echo "4. 📋 Copy the generated token"
echo "5. 🔧 Set it as environment variable"
echo ""

# Prompt for token input
read -p "📝 Please paste your SonarCloud token here (or press Enter to skip): " user_token

if [ ! -z "$user_token" ]; then
    # Validate token format (basic check)
    if [[ ${#user_token} -ge 40 ]]; then
        # Export to current session
        export SONAR_TOKEN="$user_token"
        
        # Add to shell profile for persistence
        shell_profile=""
        if [ -f "$HOME/.zshrc" ]; then
            shell_profile="$HOME/.zshrc"
        elif [ -f "$HOME/.bashrc" ]; then
            shell_profile="$HOME/.bashrc"
        elif [ -f "$HOME/.bash_profile" ]; then
            shell_profile="$HOME/.bash_profile"
        fi
        
        if [ ! -z "$shell_profile" ]; then
            # Check if SONAR_TOKEN already exists in profile
            if ! grep -q "export SONAR_TOKEN=" "$shell_profile"; then
                echo "" >> "$shell_profile"
                echo "# SonarCloud Token for wanli-backend" >> "$shell_profile"
                echo "export SONAR_TOKEN=\"$user_token\"" >> "$shell_profile"
                print_success "Token added to $shell_profile"
            else
                print_warning "SONAR_TOKEN already exists in $shell_profile"
            fi
        fi
        
        print_success "Token set successfully!"
        echo "Token preview: ${user_token:0:10}..."
    else
        print_error "Invalid token format. SonarCloud tokens are typically 40+ characters long."
        exit 1
    fi
else
    print_warning "No token provided. You can set it later using:"
    echo "export SONAR_TOKEN='your_token_here'"
fi

echo ""
print_info "Testing SonarScanner CLI with current configuration..."

# Test SonarScanner CLI
if command -v sonar-scanner &> /dev/null; then
    print_success "SonarScanner CLI is installed"
    sonar-scanner --version
else
    print_error "SonarScanner CLI is not installed or not in PATH"
    exit 1
fi

echo ""
print_info "Current project configuration:"
if [ -f "sonar-project.properties" ]; then
    print_success "sonar-project.properties found"
    echo "📄 Configuration preview:"
    head -10 sonar-project.properties | sed 's/^/   /'
else
    print_error "sonar-project.properties not found"
    exit 1
fi

echo ""
if [ ! -z "$SONAR_TOKEN" ]; then
    print_info "Ready to run SonarCloud analysis!"
    echo "🚀 You can now run: sonar-scanner"
    echo "📊 Or use Maven: ./mvnw sonar:sonar"
else
    print_warning "Please set SONAR_TOKEN before running analysis"
fi

echo ""
print_success "Setup script completed!"