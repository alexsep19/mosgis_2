define ([], function () {

    $_DO.choose_tab_property_document = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('property_document.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {
    
        query ({type: 'property_documents'}, {}, function (data) {
        
            add_vocabularies (data, {
                vc_prop_doc_types: 1,
                vc_actions: 1
            })
            
            var it = data.item
            
            it.label = it ['org.label'] || it ['person.label']

            it._can = {}

            if (($_USER.role.admin || it.uuid_org == $_USER.uuid_org) && !it.is_deleted) {
                it._can.edit   = 1
                it._can.delete = it._can.update = it._can.cancel = it._can.edit
            }
            
            $('body').data ('data', data)

            done (data)
                
        })    

    }

})