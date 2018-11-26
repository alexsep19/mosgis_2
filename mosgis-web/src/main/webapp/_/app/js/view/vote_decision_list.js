define ([], function () {

    return function (data, view) {

        $('title').text (data.item.label)

        fill (view, data.item, $('body'))

        $('#container').w2relayout ({
        
            name: 'topmost_layout',
            
            panels: [
                
                {type: 'main', size: 400,
                
                    tabs: {

                        tabs: [
                            {id: 'vote_decision_list_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_vote_decision_list

                    }                
                
                },
                
            ],
            
            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'vote_decision_list.active_tab')
            },

        });

        $(('#protocol_link')).attr({title: 'Перейти на страницу протокола'})
        clickOn ($('#protocol_link'), function () { openTab ('/voting_protocols/' + data.item.protocol_uuid) })

    }

})