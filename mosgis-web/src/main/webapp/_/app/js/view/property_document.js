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
        
        if (it.uuid_person_owner && it ['person.uuid_org'] == $_USER.uuid_org) {
        
            clickOn ($('#person_link'), function () {

                openTab ('/vc_person/' + it.uuid_person_owner)

            })
        
        }

        if (it.uuid_org_owner) {

            clickOn ($('#person_link'), function () {

                openTab ((it ['org.id_type'] > 0 ? '/voc_organization_legal/' : '/voc_organization_individual/') + it.uuid_org_owner)

            })

        }

        clickOn ($('#premise_link'), function () {
        
            function type () {
                switch (it ['p.class']) {
                    case 'ResidentialPremise': return 'premise_residental'
                    case 'NonResidentialPremise': return 'premise_nonresidental'
                    case 'Block': return 'block'
                    case 'LivingRoom': return 'living_room'
                }
            }
        
            openTab ('/' + type () + '/' + it.uuid_premise)
        
        })


    }

})