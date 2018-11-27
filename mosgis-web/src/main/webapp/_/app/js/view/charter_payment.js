define ([], function () {

    return function (data, view) {
    
        var it = data.item
        
        $('title').text (dt_dmy (it.begindate) + '-' + dt_dmy (it.enddate) + ' ' + it ['fias.label'])
        
        fill (view, it, $('body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'charter_payment_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_charter_payment

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'charter_payment.active_tab')
            },

        });

        clickOn ($('#object_link'), function () { openTab ('/charter_object/' + it.uuid_charter_object) })

        clickOn ($('#ctr_link'), function () {        
            openTab ('/charter/' + it.uuid_charter)        
        })

    }

})