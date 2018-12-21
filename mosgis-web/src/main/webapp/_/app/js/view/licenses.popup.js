define ([], function () {

    return function (data, view) {
    
        var callback = $('body').data ('licenses_popup.callback')

        $(view).w2uppop ({
        
            onClose: function () {
                callback ($_SESSION.delete ('licenses_popup.data'))
            }
        
        }, function () {
        
            $('#w2ui-popup .container').w2relayout ({

                name: 'popup_layout',

                panels: [
                    {type: 'main', size: 400},
                ],

                onRender: function (e) {
                    $_SESSION.set ('licenses_popup.on', 1)
                    use.block ('licenses')
                },
                
            });

        })

    }

})