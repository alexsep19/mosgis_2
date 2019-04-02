define ([], function () {

    return function (data, view) {
    
        var callback = $('body').data ('voc_differentiation_popup.callback')

        $(view).w2uppop ({
        
            onClose: function () {
                callback ($_SESSION.delete ('voc_differentiation_popup.data'))
            }
        
        }, function () {
        
            $('#w2ui-popup .container').w2relayout ({

                name: 'popup_layout',

                panels: [
                    {type: 'main', size: 400},
                ],

                onRender: function (e) {
                    $_SESSION.set ('voc_differentiation_popup.on', 1)
                    use.block ('voc_differentiation')
                },
                
            });

        })

    }

})