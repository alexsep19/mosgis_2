define ([], function () {

    return function (data, view) {
    
        var callback = $('body').data ('supply_resource_contracts_popup.callback')
        var postdata = $('body').data ('supply_resource_contracts_popup.post_data')

        $(view).w2uppop ({
        
            onClose: function () {
                callback ($_SESSION.delete ('supply_resource_contracts_popup.data'))
            }
        
        }, function () {
        
            $('#w2ui-popup .container').w2relayout ({

                name: 'popup_layout',

                panels: [
                    {type: 'main', size: 400},
                ],

                onRender: function (e) {
                    $_SESSION.set ('supply_resource_contracts_popup.on', 1)
                    if (postdata) {
                        $_SESSION.set('supply_resource_contracts_popup.post_data', postdata)
                    }
                    use.block ('supply_resource_contracts')
                },
                
            });

        })

    }

})