define ([], function () {

    $_DO.edit_house_docs = function (e) {
            
        use.block ('house_doc_popup')
    
    }    
    
    $_DO.create_house_docs = function (e) {
            
        if ($('body').data ('data').doc_fields.items.filter (not_off).length == 0) return alert ('Документы всех типов, доступных для этого дома, уже добавлены')

        use.block ('house_doc_new')
    
    }
    
    $_DO.download_house_docs = function (e) {    
    
        var box = $('body')

        var r = this.get (e.recid)

        function label (cur, max) {return String (Math.round (100 * cur / max)) + '%'}

        w2utils.lock (box, label (0, 1))

        download ({

            type:   'house_docs', 
            id:     e.recid,
            action: 'download',

        }, {}, {

            onprogress: function (cur, max) {$('.w2ui-lock-msg').html ('<br><br>' + label (cur, max))},

            onload: function () {w2utils.unlock (box)},

        })
    
    }

    $_DO.delete_house_docs = function (e) {    
    
        if (!e.force) return
    
        $('.w2ui-message').remove ()

        e.preventDefault ()
        
        query ({
        
            type:   'house_docs', 
            id:     w2ui [e.target].getSelection () [0],
            action: 'delete',
            
        }, {}, reload_page)
    
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var house = $('body').data ('data')

        var doc_fields = []

        query ({type: 'houses', part: 'passport_fields_common'}, {}, function (data) {

            var vocs = {}

            $.each (data.vc_pass_fields, function () {

                if (this.id_type != 3) return
                
                if (house.item ['f_' + this.id_dt]) this.off = 1
                
                var parent = house.depends [this.id]
                
                if (parent && house.item ['f_' + parent] == 0) this.off = 1
                
                doc_fields.push (this)

            })

            house.doc_fields = doc_fields
            
            add_vocabularies (house, {doc_fields: 1})
                        
            $.each (house.doc_fields.items, function () {
                this.text = this.doc_label
            })

            $('body').data ('data', house)

            done (house);

        })         

    }

})