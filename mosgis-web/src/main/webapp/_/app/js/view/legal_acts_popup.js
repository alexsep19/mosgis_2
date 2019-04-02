define ([], function () {

    return function (data, view) {
    
        var callback = $('body').data ('legal_acts_popup.callback')

        $(view).w2uppop ({
        
            onClose: function () {
                callback ($_SESSION.delete ('legal_acts_popup.data'))
            }
        
        }, function () {
        
            $('#w2ui-popup .container').w2relayout ({

                name: 'popup_layout',

                panels: [
                    {type: 'main', size: 400},
                ],

                onRender: function (e) {
                    $_SESSION.set ('legal_acts_popup.on', 1)
                    use.block ('legal_acts')
                },
                
            });

        })

    }

})