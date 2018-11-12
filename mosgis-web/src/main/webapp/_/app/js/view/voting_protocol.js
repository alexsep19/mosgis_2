define ([], function () {

    return function (data, view) {
        
        $('title').text (data.item.label)
        
        fill (view, data.item, $('body'))

        if (data.item.protocolnum == undefined || data.item.protocolnum == "") {
            $('label[id="num"]').hide ();
        }

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'voting_protocol_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_voting_protocol

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'voting_protocol.active_tab')
            },

        });

    }

})