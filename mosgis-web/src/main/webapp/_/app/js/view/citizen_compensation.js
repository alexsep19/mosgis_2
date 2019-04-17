define ([], function () {

    return function (data, view) {
    
        var it = data.item
        
        $('title').text (dt_dmy (it.begindate) + '-' + dt_dmy (it.enddate) + ' ' + it ['fias.label'])
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'citizen_compensation_common',   caption: 'Общие'},
//                            {id: 'citizen_compensation_common_', caption: 'Расчеты и перерасчеты'},
//                            {id: 'citizen_compensation_common_', caption: 'Выплаты'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_citizen_compensation

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'citizen_compensation.active_tab')
            },

        });

        clickOn ($('#person_link'), function () {
            openTab ('/vc_person/' + it.uuid_person)
        })

    }

})