name: Bug Report
description: Unwanted behavior that does not match configuration.
labels: ["Bug"]
body:
  - type: input
    attributes:
      label: Server backend version
      description: Version and type of used server software where VelocityVanish is installed.
      placeholder: Spigot 1.19.2
    validations:
      required: true
  - type: input
    attributes:
      label: Server Velocity version
      description: Version and type of used server software where VelocityVanish is installed.
      placeholder: Velocity 3.1.1 build 98
    validations:
      required: true

  - type: input
    attributes:
      label: VelocityVanish version
      description: Full version in numbers, "latest" is not a version.
      placeholder: 3.6.0
    validations:
      required: true

  - type: textarea
    attributes:
      label: Describe the bug
      description: How the plugin behaves
    validations:
      required: true

  - type: textarea
    attributes:
      label: Expected behavior
      description: How the plugin is supposed to behave instead with your configuration.
    validations:
      required: true

  - type: textarea
    attributes:
      label: Steps to reproduce
      description: What to do in order to reproduce this issue
      placeholder: |
        Step 1: ..
        Step 2: ..
    validations:
      required: true

  - type: textarea
    attributes:
      label: Additional info
      description: Any other info you want to provide, such as screenshots.
    validations:
      required: false
  - type: checkboxes
    attributes:
      label: Checklist
      description: Let's make sure this report is valid
      options:
        - label: I am running latest version of the plugin
          required: true
        - label: I have read the wiki to make sure it's not an issue with configuration
          required: true
        - label: I ticked all of the boxes without actually reading them
          required: false
