document.querySelector('.js-transition').addEventListener('click', toggle);

function toggle() {
    var container = document.querySelector('.transition-container');

    // manage clases
    var containerClasses = container.classList;
    containerClasses.toggle('js-show-pw');
    containerClasses.toggle('eye-open');
    containerClasses.toggle('eye-close');

    // update input
    var input = document.querySelector('.transition-input');

    if (containerClasses.contains('js-show-pw')) {
        // use time = animation duration
        var delay = getComputedStyle(container).getPropertyValue('--duration');
        delay = parseInt(delay);

        setTimeout(function() {
            input.classList.add('show-input');
            // update input type
            input.type="text";
        }, delay);
    } else {
        // update input type
        input.type="password";
        input.classList.remove('show-input');
    }
}