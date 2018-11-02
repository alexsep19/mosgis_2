define ([], function () {

    return function (data, view) {

        var it = data.item

        $('title').text (it.label + ' / ' + it ['h.address'] + ', ' + it ['p.label'])

        fill (view, it, $('body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'property_document_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_property_document

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'property_document.active_tab')
            },

        });
        

        clickOn ($('#house_link'), function () {
        
            openTab ('/house/' + it ['p.uuid_house'])
        
        })


    }

})