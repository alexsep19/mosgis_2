define ([], function () {

    return function (data, view) {
    
        var callback = $('body').data ('voc_licenses_popup.callback')

        $(view).w2uppop ({
        
            onClose: function () {
                callback ($_SESSION.delete ('voc_licenses_popup.data'))
            }
        
        }, function () {
        
            $('#w2ui-popup .container').w2relayout ({

                name: 'popup_layout',

                panels: [
                    {type: 'main', size: 400},
                ],

                onRender: function (e) {
                    $_SESSION.set ('voc_licenses_popup.on', 1)
                    use.block ('voc_licenses')
                },
                
            });

        })

    }

})